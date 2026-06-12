package com.yuexuan.mall.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.common.ResultCallback;
import com.yuexuan.mall.service.IDashScopeService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阿里云百炼 DashScope — DeepSeek 对话实现
 *
 * ✅ 流式输出（SSE，回调模式，累积文本逐块返回）
 * ✅ 系统提示词（System Prompt）
 * ✅ 多轮会话历史（基于 sessionId 维持上下文）
 * ✅ 前端直接替换（非追加）接收到的每块累积文本
 */
@Slf4j
@Service
public class DashScopeServiceImpl implements IDashScopeService {

    @Value("${dashscope.model:deepseek-r1}")
    private String modelName;

    private String apiKey;

    /** 系统提示词：定义 AI 的角色和行为 */
    private static final String SYSTEM_PROMPT = "你是一个专业的电商购物助手，名叫「悦选助手」，"
            + "为「悦选商城」的顾客提供帮助。你的特点：\n"
            + "1. 热情友好，用中文回答\n"
            + "2. 回答完整详细，有问必答\n"
            + "3. 熟悉电商购物流程（浏览商品、下单、支付、物流、评价、退款）\n"
            + "4. 回答简洁有条理，适当使用分段和要点\n"
            + "5. 不知道的不要编造，坦诚告知顾客\n\n"
            + "悦选商城是一个综合性电商平台，提供各类商品在线购买服务。";

    /** 会话历史存储：sessionId → 消息列表（含 system prompt + 历史对话） */
    private final Map<String, List<Message>> sessionStore = new ConcurrentHashMap<>();

    /** 每个会话最多保留的消息轮次（系统消息 + N 轮用户/助手对话） */
    private static final int MAX_HISTORY_ROUNDS = 10;

    @PostConstruct
    public void init() {
        this.apiKey = System.getenv("ALIYUN_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            this.apiKey = apiKey.trim();
            log.info("DashScope SDK 就绪，模型: {}, api-key 前6位: {}... (长度: {})",
                    modelName,
                    apiKey.substring(0, Math.min(6, apiKey.length())),
                    apiKey.length());
        } else {
            log.warn("===== ALIYUN_API_KEY 环境变量未设置 =====");
        }
    }

    @Override
    public boolean isApiKeyAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    // ════════════════════════════════════════════
    //  非流式接口（兜底）
    // ════════════════════════════════════════════

    @Override
    public String chat(String message) {
        return chat(message, null);
    }

    @Override
    public String chat(String message, String sessionId) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "😅 AI 服务未配置（环境变量 ALIYUN_API_KEY 未设置），请联系管理员配置后再使用。";
        }
        try {
            Generation gen = new Generation();
            GenerationParam param = buildParam(message, sessionId);
            GenerationResult result = gen.call(param);
            String reply = result.getOutput().getChoices().get(0).getMessage().getContent();
            // 保存到会话历史
            if (sessionId != null) {
                appendToHistory(sessionId, message, reply);
            }
            log.info("DashScope 回复成功，模型: {}, 输入: {}字, 回复: {}字",
                    modelName, message.length(), reply.length());
            return reply;
        } catch (Exception e) {
            return handleError(e);
        }
    }

    // ════════════════════════════════════════════
    //  流式接口（SSE — Server-Sent Events）
    // ════════════════════════════════════════════

    @Override
    public SseEmitter chatStream(String message) {
        return chatStream(message, null);
    }

    @Override
    public SseEmitter chatStream(String message, String sessionId) {
        // 超时 60 秒
        SseEmitter emitter = new SseEmitter(60000L);

        // 兜底定时器：超时后自动完成
        emitter.onTimeout(() -> {
            log.warn("SSE 流超时，自动结束 sessionId={}", sessionId);
            emitter.complete();
        });
        emitter.onCompletion(() ->
            log.info("SSE 流完成 sessionId={}", sessionId)
        );
        emitter.onError(ex ->
            log.error("SSE 流异常 sessionId={}: {}", sessionId, ex.getMessage())
        );

        if (apiKey == null || apiKey.isEmpty()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"type\":\"error\",\"content\":\"AI 服务未配置\"}"));
            } catch (IOException e) {
                log.error("SSE 发送失败", e);
            }
            emitter.complete();
            return emitter;
        }

        // 异步执行流式调用
        // DashScope SDK 回调模式下，每次 onEvent 收到的是完整累积文本
        final StringBuilder accumulated = new StringBuilder();
        new Thread(() -> {
            try {
                Generation gen = new Generation();
                log.info("开始 DashScope 流式调用，model={}", modelName);
                GenerationParam param = buildParam(message, sessionId);

                gen.call(param, new ResultCallback<GenerationResult>() {
                    @Override
                    public void onEvent(GenerationResult result) {
                        try {
                            String content = extractContent(result);
                            if (content == null || content.isEmpty()) {
                                return; // 跳过空 chunk（推理过程）
                            }
                            accumulated.setLength(0);
                            accumulated.append(content);
                            String json = "{\"type\":\"delta\",\"content\":"
                                    + escapeJson(content) + "}";
                            emitter.send(SseEmitter.event()
                                    .name("delta")
                                    .data(json));
                        } catch (Exception e) {
                            log.error("onEvent 处理异常: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (sessionId != null && accumulated.length() > 0) {
                            appendToHistory(sessionId, message, accumulated.toString());
                        }
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("{\"type\":\"done\"}"));
                        } catch (IOException e) {
                            log.error("SSE done 发送失败", e);
                        }
                        emitter.complete();
                    }

                    @Override
                    public void onError(Exception e) {
                        String err = e.getMessage();
                        log.error("DashScope 流式异常: {}", err);
                        // 区分已知错误类型
                        String msg = "网络繁忙，请稍后再试";
                        if (err != null && err.contains("InvalidApiKey")) {
                            msg = "API Key 无效，请检查配置";
                        } else if (err != null && err.contains("not found")) {
                            msg = "模型「" + modelName + "」未找到";
                        } else if (err != null && err.contains("InsufficientBalance")) {
                            msg = "账户余额不足";
                        } else if (err != null && err.contains("statusCode")) {
                            msg = "API 错误: " + err;
                        }
                        sendError(emitter, msg);
                    }
                });
            } catch (Exception e) {
                // ★ 任何未捕获的异常都走这里，不会杀死线程
                log.error("DashScope 线程异常: {}", e.getMessage(), e);
                sendError(emitter, "服务内部错误: " + e.getMessage());
            }
        }).start();

        return emitter;
    }

    /** 从 GenerationResult 中安全地提取文本内容 */
    private String extractContent(GenerationResult result) {
        try {
            var output = result.getOutput();
            if (output == null) return null;
            var choices = output.getChoices();
            if (choices != null && !choices.isEmpty()) {
                var msg = choices.get(0).getMessage();
                if (msg != null) {
                    String content = msg.getContent();
                    if (content != null && !content.isEmpty()) return content;
                }
            }
            // 备用：部分模型用 output.text
            String text = output.getText();
            if (text != null && !text.isEmpty()) return text;

        } catch (Exception e) {
            log.warn("extractContent 解析异常: {}", e.getMessage());
        }
        return null;
    }

    // ════════════════════════════════════════════
    //  会话历史管理
    // ════════════════════════════════════════════

    /**
     * 获取或初始化会话的消息列表
     * 每个会话的第一条永远是系统提示词
     */
    private List<Message> getOrCreateSession(String sessionId) {
        return sessionStore.computeIfAbsent(sessionId, sid -> {
            List<Message> msgs = new ArrayList<>();
            msgs.add(Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(SYSTEM_PROMPT)
                    .build());
            return msgs;
        });
    }

    /**
     * 将一轮对话追加到会话历史，并裁剪超出的轮数
     */
    private void appendToHistory(String sessionId, String userMsg, String assistantReply) {
        List<Message> history = sessionStore.get(sessionId);
        if (history == null) return;

        history.add(Message.builder()
                .role(Role.USER.getValue())
                .content(userMsg)
                .build());
        history.add(Message.builder()
                .role(Role.ASSISTANT.getValue())
                .content(assistantReply)
                .build());

        // 裁剪历史：保留 system prompt (index 0) + 最近 MAX_HISTORY_ROUNDS 轮对话
        trimHistory(history);
    }

    /**
     * 裁剪会话历史，防止 token 超限
     * 保留 system prompt（第一条）+ 最近 MAX_HISTORY_ROUNDS*2 条消息
     */
    private void trimHistory(List<Message> messages) {
        if (messages.size() <= 1 + MAX_HISTORY_ROUNDS * 2) return;

        // 保留 system prompt + 最后 MAX_HISTORY_ROUNDS*2 条消息
        Message system = messages.get(0);
        List<Message> recent = messages.subList(
                messages.size() - MAX_HISTORY_ROUNDS * 2,
                messages.size());
        messages.clear();
        messages.add(system);
        messages.addAll(recent);
    }

    // ════════════════════════════════════════════
    //  构建请求参数（含 System Prompt + 会话历史）
    // ════════════════════════════════════════════

    private GenerationParam buildParam(String userMessage, String sessionId) {
        List<Message> messages;

        if (sessionId != null) {
            // 带会话历史：从存储中获取历史消息列表
            messages = new ArrayList<>(getOrCreateSession(sessionId));
            // 追加当前用户消息
            messages.add(Message.builder()
                    .role(Role.USER.getValue())
                    .content(userMessage)
                    .build());
        } else {
            // 一次性对话：仅 system prompt + 当前消息
            messages = new ArrayList<>();
            messages.add(Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(SYSTEM_PROMPT)
                    .build());
            messages.add(Message.builder()
                    .role(Role.USER.getValue())
                    .content(userMessage)
                    .build());
        }

        return GenerationParam.builder()
                .model(modelName)
                .apiKey(apiKey)
                .messages(messages)
                .topP(0.8)
                .maxTokens(4096)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
    }

    // ════════════════════════════════════════════
    //  辅助方法
    // ════════════════════════════════════════════

    /** 发送 SSE 错误事件 */
    private void sendError(SseEmitter emitter, String msg) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"type\":\"error\",\"content\":\"" + msg + "\"}"));
            emitter.complete();
        } catch (IOException ignored) {}
    }

    /** 转义 JSON 字符串 */
    private String escapeJson(String s) {
        return "\"" + s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    /** 错误处理（非流式） */
    private String handleError(Exception e) {
        String errMsg = e.getMessage();
        log.error("DashScope 调用失败: {}", errMsg);
        if (errMsg != null && errMsg.contains("not found")) {
            return "😅 模型「" + modelName + "」未找到。当前配置: " + modelName
                    + "，请检查百炼平台模型名称是否正确。";
        }
        if (errMsg != null && errMsg.contains("InvalidApiKey")) {
            return "😅 API Key 无效，请检查 ALIYUN_API_KEY 环境变量是否正确。";
        }
        if (errMsg != null && errMsg.contains("InsufficientBalance")) {
            return "😅 账户余额不足，请充值后重试。";
        }
        return "😅 网络繁忙（" + (errMsg != null ? errMsg : "未知错误") + "），请稍后再试。";
    }
}
