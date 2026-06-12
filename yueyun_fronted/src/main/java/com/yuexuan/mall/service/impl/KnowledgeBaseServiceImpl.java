package com.yuexuan.mall.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexuan.mall.service.IKnowledgeBaseService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 知识库服务实现
 *
 * 两层存储：
 * 1. 内存：Map + 倒排索引（用于 buildKnowledgeContext / 快速检索）
 * 2. Redis：rag:product:{id} Hash + rag:keyword:{kw} Set（用于 RAG 持久化检索）
 *
 * 启动时通过 CommandLineRunner 自动加载数据到 Redis（若未存在）
 */
@Slf4j
@Service
public class KnowledgeBaseServiceImpl implements IKnowledgeBaseService, CommandLineRunner {

    // ─── 内存索引 ───
    private final Map<Long, Map<String, Object>> knowledgeMap = new LinkedHashMap<>();
    private final Map<String, List<Long>> keywordIndex = new HashMap<>();

    // ─── Redis 相关 ───
    private static final String REDIS_PRODUCT_PREFIX = "rag:product:";
    private static final String REDIS_KEYWORD_PREFIX = "rag:keyword:";
    private static final long REDIS_TTL_DAYS = 30;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, Object> redisTemplate;

    public KnowledgeBaseServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        loadKnowledge();
    }

    @Override
    public void run(String... args) {
        // CommandLineRunner：应用启动后将知识库写入 Redis
        if (knowledgeMap.isEmpty()) {
            log.warn("知识库为空，跳过 Redis 写入");
            return;
        }
        // 检查是否已经写入过（用第一个商品检查）
        Long firstId = knowledgeMap.keySet().iterator().next();
        String checkKey = REDIS_PRODUCT_PREFIX + firstId;
        Boolean exists = redisTemplate.hasKey(checkKey);
        if (Boolean.TRUE.equals(exists)) {
            log.info("Redis 知识库已存在，跳过加载");
            return;
        }

        int productCount = 0;
        int keywordCount = 0;

        // 写入商品数据到 Redis Hash
        for (Map.Entry<Long, Map<String, Object>> entry : knowledgeMap.entrySet()) {
            Long pid = entry.getKey();
            Map<String, Object> data = entry.getValue();

            String hashKey = REDIS_PRODUCT_PREFIX + pid;
            redisTemplate.opsForHash().putAll(hashKey, data);
            redisTemplate.expire(hashKey, REDIS_TTL_DAYS, TimeUnit.DAYS);
            productCount++;

            // 写入倒排索引到 Redis Set
            String categoryPath = (String) data.getOrDefault("categoryPath", "");
            String name = (String) data.getOrDefault("name", "");
            String content = (String) data.getOrDefault("content", "");

            Set<String> tokens = extractKeywords(categoryPath + " " + name + " " + content);
            for (String token : tokens) {
                String idxKey = REDIS_KEYWORD_PREFIX + token;
                redisTemplate.opsForSet().add(idxKey, pid);
                redisTemplate.expire(idxKey, REDIS_TTL_DAYS, TimeUnit.DAYS);
                keywordCount++;
            }
        }

        log.info("Redis 知识库加载完成: {} 条商品, {} 条关键词索引", productCount, keywordCount);
    }

    // ════════════════════════════════════════════
    //  内部：加载知识库数据
    // ════════════════════════════════════════════

    private void loadKnowledge() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("knowledge/知识库数据.txt");
            if (is == null) {
                log.warn("未找到 classpath:knowledge/知识库数据.txt，跳过知识库加载");
                return;
            }

            List<Map<String, Object>> items = objectMapper.readValue(
                    is, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> item : items) {
                Long productId = Long.valueOf(item.get("productId").toString());
                knowledgeMap.put(productId, item);

                String categoryPath = (String) item.getOrDefault("categoryPath", "");
                String name = (String) item.getOrDefault("name", "");
                String content = (String) item.getOrDefault("content", "");

                Set<String> tokens = extractKeywords(categoryPath + " " + name + " " + content);
                for (String token : tokens) {
                    keywordIndex.computeIfAbsent(token, k -> new ArrayList<>()).add(productId);
                }
            }

            log.info("知识库文件加载完成: {} 条商品知识", knowledgeMap.size());
        } catch (Exception e) {
            log.error("知识库加载失败: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════
    //  接口实现
    // ════════════════════════════════════════════

    @Override
    public String getKnowledgeByProductId(Long productId) {
        Map<String, Object> item = knowledgeMap.get(productId);
        if (item == null) return null;
        return String.format(
                "【商品名称】%s\n【分类路径】%s\n【商品介绍】%s",
                item.get("name"),
                item.get("categoryPath"),
                item.get("content")
        );
    }

    @Override
    public List<Long> searchProductIds(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return List.of();
        String kw = keyword.toLowerCase().trim();
        return keywordIndex.entrySet().stream()
                .filter(e -> e.getKey().contains(kw) || kw.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getAllProductIds() {
        return new ArrayList<>(knowledgeMap.keySet());
    }

    @Override
    public String buildKnowledgeContext() {
        if (knowledgeMap.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("\n\n【平台商品知识库】\n");
        sb.append("以下是本平台目前收录的商品信息，你在推荐商品时必须以此为据：\n\n");
        int idx = 1;
        for (Map<String, Object> item : knowledgeMap.values()) {
            sb.append(idx++).append(". ");
            sb.append("商品ID: ").append(item.get("productId")).append(" | ");
            sb.append("名称: ").append(item.get("name")).append(" | ");
            sb.append("分类: ").append(item.get("categoryPath")).append("\n");
            sb.append("   介绍: ").append(item.get("content")).append("\n\n");
        }
        sb.append("当用户需要商品推荐时，先匹配合适的商品ID，然后调用 queryProductById 查询详细信息。\n");
        return sb.toString();
    }

    @Override
    public boolean isLoaded() {
        return !knowledgeMap.isEmpty();
    }

    /**
     * ⭐ RAG 检索：
     * 从用户问题中提取关键词 → 查找 Redis 倒排索引 → 获取匹配的商品文档 → 拼接为上下文
     */
    @Override
    public String retrieveRelevantDocs(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) return "";
        if (knowledgeMap.isEmpty()) return "";

        // 1. 提取关键词
        Set<String> keywords = extractKeywords(userMessage);

        // 2. 先从 Redis 查找，兜底从内存索引
        Set<Long> matchedIds = new LinkedHashSet<>();
        for (String kw : keywords) {
            // Redis 倒排索引查询
            String idxKey = REDIS_KEYWORD_PREFIX + kw;
            Set<Object> members = redisTemplate.opsForSet().members(idxKey);
            if (members != null) {
                for (Object member : members) {
                    matchedIds.add(Long.valueOf(member.toString()));
                           }
                }
            // 内存索引补充
            List<Long> memMatches = keywordIndex.get(kw);
            if (memMatches != null) {
                matchedIds.addAll(memMatches);
            }
        }

        if (matchedIds.isEmpty()) return "";

        // 3. 获取匹配文档内容
        StringBuilder sb = new StringBuilder("\n\n【相关知识库信息】\n");
        sb.append("根据你的问题，以下商品可能与你相关：\n\n");

        int count = 0;
        for (Long pid : matchedIds) {
            if (count >= 5) break; // 最多取前 5 条
            Map<String, Object> doc = knowledgeMap.get(pid);
            if (doc == null) continue;

            sb.append("- 商品ID: ").append(pid)
                    .append(" | ").append(doc.get("name"))
                    .append(" | ").append(doc.get("categoryPath")).append("\n");
            sb.append("  介绍: ").append(doc.get("content")).append("\n\n");
            count++;
        }

        sb.append("请根据以上知识，结合用户的问题给出专业、有针对性的回答。\n");
        return sb.toString();
    }

    // ════════════════════════════════════════════
    //  工具方法
    // ════════════════════════════════════════════

    /** 从中文文本中提取关键词（含单字 + 二元组） */
    private Set<String> extractKeywords(String text) {
        if (text == null || text.isEmpty()) return Set.of();

        Set<String> tokens = new LinkedHashSet<>();
        // 去除标点符号，按空白和常见分隔符切分
        String clean = text.toLowerCase()
                .replaceAll("[\\s,./;:！？，。；：、\"\"''（）()【】《》/\\-+*#@]", " ");

        String[] parts = clean.split("\\s+");
        for (String part : parts) {
            if (part.length() >= 2) {
                tokens.add(part); // 完整词
            }
            // 对中文: 2-gram 二元组
            if (part.length() >= 2 && part.length() <= 6) {
                tokens.add(part);
            }
        }

        // 对于中文字符串，提取所有长度 ≥ 2 的连续子串
        StringBuilder chineseBuf = new StringBuilder();
        for (char c : clean.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF) {
                chineseBuf.append(c);
            }
        }
        String chinese = chineseBuf.toString();
        for (int i = 0; i < chinese.length(); i++) {
            for (int j = i + 2; j <= Math.min(i + 6, chinese.length()); j++) {
                tokens.add(chinese.substring(i, j));
            }
        }

        return tokens;
    }
}
