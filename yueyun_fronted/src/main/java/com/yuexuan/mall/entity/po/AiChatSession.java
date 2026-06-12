package com.yuexuan.mall.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 客服会话记录表
 */
@Data
@TableName("ai_chat_session")
public class AiChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话标识 UUID */
    private String sessionId;

    /** 用户ID */
    private Long userId;

    /** 会话标题 */
    private String title;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 最后操作人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
