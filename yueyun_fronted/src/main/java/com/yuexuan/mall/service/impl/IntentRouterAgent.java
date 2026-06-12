package com.yuexuan.mall.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 意图路由智能体
 * 使用极简 prompt 调用 AI，仅返回意图代码（RECOMMEND/ORDER/LOGISTICS/AFTERSALE/CONSULT/GENERAL）
 */
@Slf4j
@Component
public class IntentRouterAgent {

    private final ChatClient intentClient;

    public IntentRouterAgent(ChatClient.Builder builder, AgentPromptFactory promptFactory) {
        String intentPrompt = promptFactory.getPrompt("INTENT");
        this.intentClient = builder
                .defaultSystem(intentPrompt)
                .build();
    }

    /**
     * 分析用户意图
     * @param userMessage 用户最新消息
     * @return 意图代码：RECOMMEND / ORDER / LOGISTICS / AFTERSALE / CONSULT / GENERAL
     */
    public String analyzeIntent(String userMessage) {
        try {
            String result = intentClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();

            if (result != null) {
                result = result.trim().toUpperCase();
                // 只取第一个字母数字代码
                String[] parts = result.split("[^A-Z]");
                for (String p : parts) {
                    if (p.matches("RECOMMEND|ORDER|LOGISTICS|AFTERSALE|CONSULT|GENERAL")) {
                        log.info("意图路由结果: {} (原始: {})", p, result);
                        return p;
                    }
                }
            }
        } catch (Exception e) {
            log.error("意图分析失败: {}", e.getMessage());
        }
        return "GENERAL";
    }
}
