package com.yuexuan.mall.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexuan.mall.service.IKnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 客服核心服务实现（多智能体路由版）
 *
 * 架构：
 * 1. IntentRouterAgent 分析意图 → 分发对应专家智能体
 * 2. 专家智能体绑定的提示词 + 知识库 + RAG → 构建 System Prompt
 * 3. 调用 AI（注册所有 Tool 函数）
 * 4. 分块输出 1001 文本 → 1003 商品卡片 → 1002 结束
 * 5. 首轮对话完成后异步生成标题
 */
@Slf4j
@Service
public class AiCustomerServiceImpl implements com.yuexuan.mall.service.IAiCustomerService {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;
    private final IKnowledgeBaseService knowledgeBaseService;
    private final ToolResultHolder toolResultHolder;
    private final IntentRouterAgent intentRouterAgent;
    private final AgentPromptFactory agentPromptFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 知识库上下文 */
    private final String knowledgeContext;

    /** 旧全能型提示词（用于标题生成） */
    private final String generalPrompt;

    /** 停止标记 */
    private final Map<String, AtomicBoolean> stopFlags = new HashMap<>();

    /** 已生成标题的会话 */
    private final Set<String> titleGenerated = new HashSet<>();

    /** 会话 → 首条消息映射 */
    private final Map<String, String> sessionFirstMessage = new HashMap<>();

    private final com.yuexuan.mall.mapper.AiChatSessionMapper aiChatSessionMapper;

    public AiCustomerServiceImpl(
            ChatClient.Builder chatClientBuilder,
            @Qualifier("redisChatMemory") ChatMemory chatMemory,
            IKnowledgeBaseService knowledgeBaseService,
            ToolResultHolder toolResultHolder,
            IntentRouterAgent intentRouterAgent,
            AgentPromptFactory agentPromptFactory,
            com.yuexuan.mall.mapper.AiChatSessionMapper aiChatSessionMapper) {

        this.chatClientBuilder = chatClientBuilder;
        this.chatMemory = chatMemory;
        this.knowledgeBaseService = knowledgeBaseService;
        this.toolResultHolder = toolResultHolder;
        this.intentRouterAgent = intentRouterAgent;
        this.agentPromptFactory = agentPromptFactory;
        this.aiChatSessionMapper = aiChatSessionMapper;
        this.knowledgeContext = knowledgeBaseService.buildKnowledgeContext();

        // 加载旧全能型提示词（用于标题生成 + GENERAL fallback）
        this.generalPrompt = loadGeneralPrompt();

        log.info("AiCustomerServiceImpl 初始化完成（多智能体模式），generalPrompt 长度: {}", generalPrompt.length());
    }

    private String loadGeneralPrompt() {
        try {
            var resource = new ClassPathResource("prompt.txt");
            if (resource.exists()) {
                return resource.getContentAsString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("加载 prompt.txt 失败: {}", e.getMessage());
        }
        return "你是一个专业的电商购物助手，名叫「悦选助手」。";
    }

    // ════════════════════════════════════════════
    //  接口实现
    // ════════════════════════════════════════════

    @Override
    public SseEmitter chatStream(String message, String sessionId, Long userId) {
        SseEmitter emitter = new SseEmitter(120_000L);

        emitter.onTimeout(() -> { log.warn("SSE 超时 sessionId={}", sessionId); emitter.complete(); });
        emitter.onCompletion(() -> { log.info("SSE 完成 sessionId={}", sessionId); stopFlags.remove(sessionId); });
        emitter.onError(ex -> log.error("SSE 异常 sessionId={}: {}", sessionId, ex.getMessage()));

        if (sessionId != null && userId != null) {
            sessionFirstMessage.putIfAbsent(sessionId, message);
        }

        new Thread(() -> {
            try { doChatStream(message, sessionId, userId, emitter); }
            catch (Exception e) { log.error("chatStream 异常: {}", e.getMessage(), e); sendError(emitter, "服务繁忙，请稍后再试"); }
        }).start();

        return emitter;
    }

    @Override
    public void stopGeneration(String sessionId, Long userId) {
        if (sessionId != null)
            stopFlags.computeIfAbsent(sessionId, k -> new AtomicBoolean(false)).set(true);
    }

    @Override
    public boolean isStopped(String sessionId, Long userId) {
        AtomicBoolean flag = stopFlags.get(sessionId);
        return flag != null && flag.get();
    }

    // ════════════════════════════════════════════
    //  标题生成（同步版 + 异步兼容版）
    // ════════════════════════════════════════════

    @Override
    public void generateSessionTitleAsync(String sessionId, String aiReply) {
        // 旧方法保留兼容
        generateSessionTitleAsync(sessionId, aiReply, sessionFirstMessage.get(sessionId));
    }

    public void generateSessionTitleAsync(String sessionId, String aiReply, String firstMessage) {
        if (sessionId == null || aiReply == null || aiReply.length() < 5) return;
        String title = generateSessionTitleSync(sessionId, aiReply, firstMessage);
        if (title != null) {
            log.info("异步标题已生成: {} → {}", sessionId, title);
        }
    }

    /**
     * 同步生成会话标题 — 将首轮对话内容发给大模型总结标题
     * @return 生成的标题（不含时间戳），若失败返回 null
     */
    private String generateSessionTitleSync(String sessionId, String aiReply, String firstMessage) {
        if (aiReply == null || aiReply.length() < 5) return null;
        // 防止数据库已有标题的跳过
        try {
            var checkWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                    com.yuexuan.mall.entity.po.AiChatSession>()
                    .eq(com.yuexuan.mall.entity.po.AiChatSession::getSessionId, sessionId);
            var existing = aiChatSessionMapper.selectOne(checkWrapper);
            if (existing != null && existing.getTitle() != null
                    && !"新对话".equals(existing.getTitle())
                    && existing.getTitle().contains("_")) {
                log.info("标题已存在，跳过生成: {}", existing.getTitle());
                return null;
            }
        } catch (Exception e) {
            log.warn("检查已有标题失败: {}", e.getMessage());
        }

        String firstMsg = (firstMessage != null) ? firstMessage : sessionFirstMessage.get(sessionId);
        if (firstMsg == null) firstMsg = "";

        try {
            ChatClient titleClient = chatClientBuilder
                    .defaultSystem(generalPrompt + "\n\n请根据以上角色，基于用户的问题和你的回复，生成一个不超过15个字的简洁标题，只输出标题文字，不要加引号、标点、多余字符。")
                    .build();

            String promptText = "用户第一次提问：" + firstMsg
                    + "\n\n你的第一次回复：" + aiReply
                    + "\n\n请根据以上对话生成一个不超过15个字的标题，只输出标题本身。";

            String title = titleClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            if (title == null || title.trim().isEmpty()) return null;
            title = title.trim().replaceAll("[\"\"'']", "");
            if (title.length() > 20) title = title.substring(0, 20);
            if (title.length() < 2) return null;

            // 拼接时间戳并更新数据库
            String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmm")
                    .format(new java.util.Date());
            String finalTitle = title + "_" + timestamp;

            var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                    com.yuexuan.mall.entity.po.AiChatSession>()
                    .eq(com.yuexuan.mall.entity.po.AiChatSession::getSessionId, sessionId);
            var session = aiChatSessionMapper.selectOne(wrapper);
            if (session != null) {
                session.setTitle(finalTitle);
                session.setUpdateTime(java.time.LocalDateTime.now());
                aiChatSessionMapper.updateById(session);
                log.info("会话标题已生成: {} → {}", sessionId, finalTitle);
            }
            return title;
        } catch (Exception e) {
            log.warn("标题生成失败: {}", e.getMessage());
            return null;
        }
    }

    // ════════════════════════════════════════════
    //  核心流程
    // ════════════════════════════════════════════

    private void doChatStream(String message, String sessionId, Long userId, SseEmitter emitter) {
        // 1. 意图路由
        String intentCode = intentRouterAgent.analyzeIntent(message);
        log.info("意图路由: sessionId={}, intent={}", sessionId, intentCode);

        // 2. RAG 检索
        String ragContext = knowledgeBaseService.retrieveRelevantDocs(message);
        boolean hasRagDocs = !ragContext.isEmpty();

        // 3. 加载专家 System Prompt
        String agentPrompt;
        if ("GENERAL".equals(intentCode)) {
            agentPrompt = generalPrompt; // GENERAL 用全能型提示词
        } else {
            agentPrompt = agentPromptFactory.getPrompt(intentCode);
        }
        String fullSystemPrompt = agentPrompt + knowledgeContext;
        if (hasRagDocs) fullSystemPrompt += ragContext;

        // 4. 构建对话历史
        List<Message> history = new ArrayList<>();
        if (sessionId != null) history.addAll(chatMemory.get(sessionId, 100));
        history.add(new UserMessage(message));

        // 5. 创建临时 ChatClient（注册所有 Tool）
        ChatClient sessionClient = chatClientBuilder
                .defaultSystem(fullSystemPrompt)
                .build();

        // 6. 同步调用 AI（含所有工具函数）
        toolResultHolder.clear();
        toolResultHolder.setCurrentUserId(userId); // ★ 供 Tool 函数使用
        String replyText = "";

        try {
            replyText = sessionClient.prompt()
                    .messages(history)
                    .tools("queryProduct", "queryProductById", "prePlaceOrder",
                            "queryOrderByNo", "queryUserOrders", "cancelOrder", "createReturnOrder")
                    .call()
                    .content();

            if (replyText == null || replyText.isEmpty()) {
                replyText = "抱歉，我没有理解您的意思，可以换一种说法吗？";
            }
            log.info("AI 回复 (intent={}) 长度: {} 字", intentCode, replyText.length());
        } catch (Exception e) {
            log.error("AI 调用失败: {}", e.getMessage(), e);
            sendError(emitter, "AI 回复生成失败，请稍后再试");
            return;
        }

        // 7. 分块输出 1001 文本
        StringBuilder streamedText = new StringBuilder();
        int pos = 0;
        int chunkSize = 5;
        while (pos < replyText.length()) {
            if (isStopped(sessionId, userId)) break;
            int end = Math.min(pos + chunkSize, replyText.length());
            String chunk = replyText.substring(pos, end);
            streamedText.append(chunk);
            try {
                emitter.send(SseEmitter.event().name("delta")
                        .data("{\"eventType\":1001,\"eventData\":" + escapeJson(chunk) + "}"));
            } catch (IOException e) { break; }
            pos = end;
            if (pos < replyText.length()) {
                try { Thread.sleep(25); } catch (InterruptedException ignored) { break; }
            }
        }

        // 8. 输出 1003 商品卡片
        if (toolResultHolder.hasProducts()) {
            try {
                String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                Map<String, Object> wrapper = new LinkedHashMap<>();
                wrapper.put("requestId", requestId);
                wrapper.put("products", toolResultHolder.getProducts());
                String eventData = objectMapper.writeValueAsString(wrapper);
                emitter.send(SseEmitter.event().name("delta")
                        .data("{\"eventType\":1003,\"eventData\":" + eventData + "}"));
            } catch (Exception e) {
                log.warn("发送 1003 事件失败: {}", e.getMessage());
            }
        }

        if (isStopped(sessionId, userId)) { emitter.complete(); return; }

        // 9. 保存会话记忆
        if (sessionId != null && streamedText.length() > 0) {
            chatMemory.add(sessionId, List.of(
                    new UserMessage(message),
                    new AssistantMessage(streamedText.toString())
            ));
        }

        // 10. 生成标题（首轮对话同步生成，确保 done 事件时标题已就绪）
        String generatedTitle = null;
        if (sessionId != null && !titleGenerated.contains(sessionId) && streamedText.length() > 5) {
            titleGenerated.add(sessionId); // 同步拦截，防止并发重复
            generatedTitle = generateSessionTitleSync(sessionId, streamedText.toString(), sessionFirstMessage.get(sessionId));
        }

        // 11. 结束事件（含标题）
        try {
            StringBuilder doneData = new StringBuilder("{\"eventType\":1002");
            if (generatedTitle != null) {
                doneData.append(",\"title\":").append(escapeJson(generatedTitle));
            }
            doneData.append("}");
            emitter.send(SseEmitter.event().name("done").data(doneData.toString()));
        } catch (IOException e) { log.error("SSE done 失败", e); }
        emitter.complete();
    }

    // ════════════════════════════════════════════
    //  辅助方法
    // ════════════════════════════════════════════

    private void sendError(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event().name("error")
                    .data("{\"eventType\":1003,\"eventData\":\"" + msg + "\"}"));
        } catch (IOException ignored) {}
        emitter.complete();
    }

    private String escapeJson(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }
}
