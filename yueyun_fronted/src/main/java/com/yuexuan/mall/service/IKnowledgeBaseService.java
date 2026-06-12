package com.yuexuan.mall.service;

import java.util.List;
import java.util.Map;

/**
 * 知识库服务 — 加载 知识库数据.txt 中的商品知识
 */
public interface IKnowledgeBaseService {

    /** 根据商品ID查询知识库中的商品知识文本 */
    String getKnowledgeByProductId(Long productId);

    /** 根据关键词搜索知识库中的商品ID */
    List<Long> searchProductIds(String keyword);

    /** 获取所有知识库商品ID */
    List<Long> getAllProductIds();

    /** 获取知识库中的完整商品上下文（卖给 AI 的文本） */
    String buildKnowledgeContext();

    /** 判断知识库是否已加载 */
    boolean isLoaded();

    /** ⭐ RAG：根据用户问题检索相关文档，返回上下文文本，已注入到 Redis */
    String retrieveRelevantDocs(String userMessage);
}
