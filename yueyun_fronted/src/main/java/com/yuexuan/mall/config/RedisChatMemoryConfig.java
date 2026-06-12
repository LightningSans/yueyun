package com.yuexuan.mall.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 会话记忆存储
 *
 * key 格式: chat:memory:{userId}:{sessionId}
 * 消息以 JSON 存为 Redis List，两两一组（user + assistant）
 * 每个会话最多保留 100 条消息（50 轮），7 天过期
 */
@Slf4j
@Configuration
public class RedisChatMemoryConfig {

    /** 最大消息数（含 user + assistant） */
    private static final int MAX_MESSAGES = 100;

    /** 过期时间（天） */
    private static final int TTL_DAYS = 7;

    @Bean
    public ChatMemory redisChatMemory(StringRedisTemplate redisTemplate) {
        ObjectMapper mapper = new ObjectMapper();

        return new ChatMemory() {
            @Override
            public List<Message> get(String sessionId, int lastN) {
                String key = "chat:memory:" + sessionId;
                List<String> jsons = redisTemplate.opsForList().range(key, -lastN, -1);
                if (jsons == null || jsons.isEmpty()) return List.of();

                return jsons.stream().map(json -> {
                    try {
                        Map<String, Object> map = mapper.readValue(json, Map.class);
                        String role = (String) map.get("role");
                        String text = (String) map.get("text");
                        if ("user".equals(role)) return new UserMessage(text);
                        if ("assistant".equals(role)) return new AssistantMessage(text);
                        return null;
                    } catch (Exception e) {
                        log.warn("反序列化消息失败: {}", e.getMessage());
                        return null;
                    }
                }).filter(m -> m != null).collect(Collectors.toList());
            }

            @Override
            public void add(String sessionId, List<Message> messages) {
                String key = "chat:memory:" + sessionId;

                for (Message msg : messages) {
                    try {
                        Map<String, Object> map = Map.of(
                                "role", msg.getMessageType() == MessageType.USER ? "user" : "assistant",
                                "text", msg.getText()
                        );
                        String json = mapper.writeValueAsString(map);
                        redisTemplate.opsForList().rightPush(key, json);
                    } catch (JsonProcessingException e) {
                        log.warn("序列化消息失败: {}", e.getMessage());
                    }
                }

                // 裁剪超出的消息
                Long size = redisTemplate.opsForList().size(key);
                if (size != null && size > MAX_MESSAGES) {
                    redisTemplate.opsForList().trim(key, size - MAX_MESSAGES, -1);
                }

                // 设置 7 天过期
                redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS);
            }

            @Override
            public void clear(String sessionId) {
                redisTemplate.delete("chat:memory:" + sessionId);
            }
        };
    }
}
