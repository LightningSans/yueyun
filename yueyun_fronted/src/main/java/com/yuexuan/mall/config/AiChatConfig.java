package com.yuexuan.mall.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 配置
 *
 * 不创建默认 ChatClient Bean，由 AiCustomerServiceImpl 自行
 * 通过 ChatClient.Builder 构建带 system prompt 和 tool 的临时 client。
 *
 * 保留此配置仅为占位，避免误注入。
 */
@Configuration
public class AiChatConfig {
    // ChatClient.Builder 由 spring-ai-openai-spring-boot-starter 自动配置
    // AiCustomerServiceImpl 自行注入 Builder 构建客户端
}
