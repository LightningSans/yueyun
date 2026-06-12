package com.yuexuan.mall.entity.po;

import java.math.BigDecimal;
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
 * 商品表
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品标题（用户端展示）
     */
    private String name;

    /**
     * 所属分类ID（关联分类表）
     */
    private Long categoryId;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 销售价格（必填）
     */
    private BigDecimal price;

    /**
     * 原价/划线价（用于促销展示）
     */
    private BigDecimal originalPrice;

    /**
     * 当前库存数量
     */
    private Integer stock;

    /**
     * 累计销量（默认0，下单时递增）
     */
    private Integer sales;

    /**
     * 商品图片列表（JSON数组，第一张为主图）
     */
    private String images;

    /**
     * 商品详细描述（文本）
     */
    private String description;

    /**
     * 商品规格定义（JSON）
     */
    private String specs;

    /**
     * 搜索关键词，逗号分隔
     */
    private String keywords;

    /**
     * 状态：1-上架，0-下架
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
     * 最后更新时间
     */
    private LocalDateTime updateTime;


}
