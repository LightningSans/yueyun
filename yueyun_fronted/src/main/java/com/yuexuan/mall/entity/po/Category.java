package com.yuexuan.mall.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 分类表
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称（如"手机"、"电脑"）
     */
    private String name;

    /**
     * 父分类ID，0表示顶级分类
     */
    private Long parentId;

    /**
     * 排序值（数值越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建人ID（关联 admin.id）
     */
    private Long createBy;

    /**
     * 最后操作人ID（关联 admin.id）
     */
    private Long updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
