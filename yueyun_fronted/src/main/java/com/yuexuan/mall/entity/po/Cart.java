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
 * 购物车表
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cart")
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（买家）
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称（冗余，加入时快照）
     */
    private String productName;

    /**
     * 商品主图（冗余，快照）
     */
    private String productImage;

    /**
     * 加入时的单价（冗余，避免下单时价格变动争议）
     */
    private BigDecimal price;

    /**
     * 购买数量（≥1）
     */
    private Integer quantity;

    /**
     * 是否勾选：1-选中，0-未选
     */
    private Integer selected;

    /**
     * 创建人ID（关联 user.id）
     */
    private Long createBy;

    /**
     * 最后操作人ID（关联 user.id）
     */
    private Long updateBy;

    /**
     * 加入时间
     */
    private LocalDateTime createTime;

    /**
     * 最后修改时间
     */
    private LocalDateTime updateTime;


}
