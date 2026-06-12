package com.yuexuan.mall.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 阿里云百炼 DashScope AI 对话服务
 */
public interface IDashScopeService {

    /**
     * 非流式对话（全量返回）
     * @param message 用户消息
     * @deprecated 推荐使用支持会话历史的 {@link #chat(String, String)}
     */
    String chat(String message);

    /**
     * 非流式对话（全量返回，带会话历史）
     * @param message   用户消息
     * @param sessionId 会话ID（用于维持上下文，null 则视为一次性对话）
     */
    String chat(String message, String sessionId);

    /**
     * 流式对话（SSE 逐字返回）
     * @param message 用户消息
     * @deprecated 推荐使用支持会话历史的 {@link #chatStream(String, String)}
     */
    SseEmitter chatStream(String message);

    /**
     * 流式对话（SSE 逐字返回，带会话历史）
     * @param message   用户消息
     * @param sessionId 会话ID（用于维持上下文，null 则视为一次性对话）
     */
    SseEmitter chatStream(String message, String sessionId);

    /** 检查 API Key 是否已配置 */
    boolean isApiKeyAvailable();

    /** 获取当前使用的模型名 */
    String getModelName();
}
