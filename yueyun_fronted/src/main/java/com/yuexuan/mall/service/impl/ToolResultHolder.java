package com.yuexuan.mall.service.impl;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tool 调用结果持有者
 * 在调用 AI 前 clear，Function 写入，调用后读取
 */
@Component
public class ToolResultHolder {

    private List<Map<String, Object>> productResults = new ArrayList<>();
    private Long currentUserId;

    public void setProducts(List<Map<String, Object>> products) {
        this.productResults = products != null ? products : new ArrayList<>();
    }

    public List<Map<String, Object>> getProducts() {
        return productResults;
    }

    public boolean hasProducts() {
        return productResults != null && !productResults.isEmpty();
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
    }

    public void clear() {
        productResults = new ArrayList<>();
        currentUserId = null;
    }
}
