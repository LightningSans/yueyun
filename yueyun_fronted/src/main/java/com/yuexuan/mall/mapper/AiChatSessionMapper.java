package com.yuexuan.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuexuan.mall.entity.po.AiChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 客服会话记录 Mapper
 */
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {
}
