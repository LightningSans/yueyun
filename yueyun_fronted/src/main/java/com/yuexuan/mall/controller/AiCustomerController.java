package com.yuexuan.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.AiChatSession;
import com.yuexuan.mall.mapper.AiChatSessionMapper;
import com.yuexuan.mall.security.JwtTokenProvider;
import com.yuexuan.mall.service.IAiCustomerService;
import com.yuexuan.mall.service.IHotQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI 客服对话接口（多智能体路由 + 会话管理）
 */
@Slf4j
@Tag(name = "AI 客服")
@RestController
@RequestMapping("/api/ai")
public class AiCustomerController {

    private final IAiCustomerService aiCustomerService;
    private final IHotQuestionService hotQuestionService;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public AiCustomerController(
            IAiCustomerService aiCustomerService,
            IHotQuestionService hotQuestionService,
            AiChatSessionMapper aiChatSessionMapper,
            StringRedisTemplate redisTemplate,
            JwtTokenProvider jwtTokenProvider) {
        this.aiCustomerService = aiCustomerService;
        this.hotQuestionService = hotQuestionService;
        this.aiChatSessionMapper = aiChatSessionMapper;
        this.redisTemplate = redisTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // ════════════════════════════════════════════
    //  对话
    // ════════════════════════════════════════════

    @Operation(summary = "AI 客服对话（流式 SSE，多智能体路由）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        String message = request.getMessage();
        if (message == null || message.trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data("{\"eventType\":1003,\"eventData\":\"消息不能为空\"}"));
            } catch (Exception ignored) {}
            emitter.complete();
            return emitter;
        }

        Long userId = resolveUserId(httpRequest);
        String sessionId = request.getSessionId();
        boolean isNew = (sessionId == null || sessionId.isEmpty());
        if (isNew) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        log.info("AI 客服: sessionId={}, userId={}, msgLen={}", sessionId, userId, message.length());

        if (isNew && userId != null) {
            saveSession(sessionId, userId);
        }

        return aiCustomerService.chatStream(message, sessionId, userId);
    }

    // ════════════════════════════════════════════
    //  停止生成
    // ════════════════════════════════════════════

    @Operation(summary = "停止 AI 生成")
    @PostMapping("/stop")
    public R<Void> stop(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        Long userId = resolveUserId(httpRequest);
        String sessionId = request.getSessionId();
        if (sessionId == null) return R.failed("sessionId 不能为空");

        String flagKey = "chat:stop:" + (userId != null ? userId : "0") + ":" + sessionId;
        redisTemplate.opsForValue().set(flagKey, "1", 60, TimeUnit.SECONDS);
        log.info("AI 停止生成: {}", flagKey);

        aiCustomerService.stopGeneration(sessionId, userId);
        return R.success(null);
    }

    // ════════════════════════════════════════════
    //  热点问题
    // ════════════════════════════════════════════

    @Operation(summary = "获取热点问题")
    @GetMapping("/hot-questions")
    public R<List<Map<String, String>>> hotQuestions(@RequestParam(defaultValue = "3") int limit) {
        return R.success(hotQuestionService.getHotQuestions(limit));
    }

    // ════════════════════════════════════════════
    //  会话列表（按时间分组）
    // ════════════════════════════════════════════

    @Operation(summary = "查询会话列表（按时间分组）")
    @GetMapping("/sessions")
    public R<Map<String, Object>> sessions(HttpServletRequest httpRequest) {
        Long userId = resolveUserId(httpRequest);
        if (userId == null) return R.failed("请先登录");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeMonthsAgo = now.minusMonths(3);

        // 查询 3 个月内的会话
        List<AiChatSession> allSessions = aiChatSessionMapper.selectList(
                new LambdaQueryWrapper<AiChatSession>()
                        .eq(AiChatSession::getUserId, userId)
                        .ge(AiChatSession::getCreateTime, threeMonthsAgo)
                        .orderByDesc(AiChatSession::getCreateTime)
                        .last("LIMIT 50"));

        // 按 create_time 分组
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekAgo = now.minusDays(7);

        List<Map<String, Object>> todayList = new ArrayList<>();
        List<Map<String, Object>> weekList = new ArrayList<>();
        List<Map<String, Object>> monthList = new ArrayList<>();

        for (AiChatSession s : allSessions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("sessionId", s.getSessionId());
            item.put("title", s.getTitle());
            item.put("createTime", s.getCreateTime() != null ? s.getCreateTime().toString() : null);
            item.put("updateTime", s.getUpdateTime() != null ? s.getUpdateTime().toString() : null);

            LocalDateTime ct = s.getCreateTime();
            if (ct != null) {
                if (ct.isAfter(todayStart)) {
                    todayList.add(item);
                } else if (ct.isAfter(weekAgo)) {
                    weekList.add(item);
                } else {
                    monthList.add(item);
                }
            } else {
                monthList.add(item);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("today", todayList);
        result.put("week", weekList);
        result.put("month", monthList);

        return R.success(result);
    }

    // ════════════════════════════════════════════
    //  修改标题
    // ════════════════════════════════════════════

    @Operation(summary = "修改会话标题")
    @PutMapping("/session/{sessionId}/title")
    public R<Void> updateTitle(@PathVariable String sessionId,
                               @RequestBody Map<String, String> body,
                               HttpServletRequest httpRequest) {
        Long userId = resolveUserId(httpRequest);
        if (userId == null) return R.failed("请先登录");

        String newTitle = body.get("title");
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return R.failed("标题不能为空");
        }

        newTitle = newTitle.trim();
        // 拼接时间戳
        String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmm")
                .format(new java.util.Date());
        String finalTitle = newTitle + "_" + timestamp;

        var session = aiChatSessionMapper.selectOne(
                new LambdaQueryWrapper<AiChatSession>()
                        .eq(AiChatSession::getSessionId, sessionId)
                        .eq(AiChatSession::getUserId, userId));
        if (session == null) return R.failed("会话不存在");

        session.setTitle(finalTitle);
        session.setUpdateTime(LocalDateTime.now());
        aiChatSessionMapper.updateById(session);
        log.info("标题已修改: {} → {}", sessionId, finalTitle);

        return R.success(null);
    }

    // ════════════════════════════════════════════
    //  定时清理（每天凌晨 2 点执行）
    // ════════════════════════════════════════════

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldSessions() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        int deleted = aiChatSessionMapper.delete(
                new LambdaQueryWrapper<AiChatSession>()
                        .lt(AiChatSession::getCreateTime, threeMonthsAgo));
        if (deleted > 0) {
            log.info("定时清理: 删除了 {} 条 3 个月前的会话记录", deleted);
        }
    }

    // ════════════════════════════════════════════
    //  内部方法
    // ════════════════════════════════════════════

    private Long resolveUserId(HttpServletRequest request) {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = jwtTokenProvider.resolveToken(header);
                if (token != null && jwtTokenProvider.validateToken(token)) {
                    return jwtTokenProvider.getUserId(token);
                }
            }
        } catch (Exception e) {
            log.debug("匿名访问: {}", e.getMessage());
        }
        return null;
    }

    private void saveSession(String sessionId, Long userId) {
        try {
            AiChatSession session = new AiChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setTitle("新对话");
            session.setCreateBy(userId);
            session.setUpdateBy(userId);
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());
            aiChatSessionMapper.insert(session);
        } catch (Exception e) {
            log.warn("保存会话失败: {}", e.getMessage());
        }
    }

    public static class ChatRequest {
        private String message;
        private String sessionId;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}
