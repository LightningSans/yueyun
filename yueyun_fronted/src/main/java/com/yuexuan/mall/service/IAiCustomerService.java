package com.yuexuan.mall.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 客服核心服务
 */
public interface IAiCustomerService {

    /**
     * 流式对话（SSE）- 含多智能体路由
     */
    SseEmitter chatStream(String message, String sessionId, Long userId);

    /**
     * 停止生成
     */
    void stopGeneration(String sessionId, Long userId);

    /**
     * 判断当前会话是否被标记为停止
     */
    boolean isStopped(String sessionId, Long userId);

    /**
     * 异步生成会话标题
     */
    void generateSessionTitleAsync(String sessionId, String aiReply);
}
