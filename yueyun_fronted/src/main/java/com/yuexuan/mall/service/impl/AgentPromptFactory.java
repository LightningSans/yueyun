package com.yuexuan.mall.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 智能体提示词工厂
 * 根据意图代码加载对应的专家智能体提示词
 */
@Slf4j
@Component
public class AgentPromptFactory {

    private final Map<String, String> agentPrompts = new HashMap<>();
    private final Map<String, String> promptFiles = new HashMap<>();

    public AgentPromptFactory() {
        promptFiles.put("INTENT", "prompt_intent.txt");
        promptFiles.put("RECOMMEND", "prompt_recommend.txt");
        promptFiles.put("ORDER", "prompt_order.txt");
        promptFiles.put("LOGISTICS", "prompt_logistics.txt");
        promptFiles.put("AFTERSALE", "prompt_aftersale.txt");
        promptFiles.put("CONSULT", "prompt_consult.txt");
        promptFiles.put("GENERAL", "prompt.txt");
    }

    @PostConstruct
    public void init() {
        for (var entry : promptFiles.entrySet()) {
            String intent = entry.getKey();
            String file = entry.getValue();
            String prompt = loadPrompt(file);
            agentPrompts.put(intent, prompt);
            log.info("智能体 [{}] 提示词已加载: {} ({}字符)", intent, file, prompt.length());
        }
    }

    /**
     * 根据意图代码获取对应的专家提示词
     */
    public String getPrompt(String intentCode) {
        return agentPrompts.getOrDefault(intentCode, agentPrompts.get("GENERAL"));
    }

    private String loadPrompt(String fileName) {
        try {
            var resource = new ClassPathResource(fileName);
            if (resource.exists()) {
                return resource.getContentAsString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("加载 {} 失败: {}", fileName, e.getMessage());
        }
        return "你是一个专业的电商购物助手。";
    }
}
