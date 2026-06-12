package com.yuexuan.mall.service;

import java.util.List;
import java.util.Map;

/**
 * 热点问题服务
 */
public interface IHotQuestionService {

    /** 获取前 N 条热点问题 */
    List<Map<String, String>> getHotQuestions(int limit);

    /** 获取所有热点问题 */
    List<Map<String, String>> getAllHotQuestions();
}
