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
 * 订单明细表（只读快照，无操作人字段）
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID（关联 order.id）
     */
    private Long orderId;

    /**
     * 订单号（冗余，方便单独查询明细）
     */
    private String orderNo;

    /**
     * 商品ID（溯源用）
     */
    private Long productId;

    /**
     * 商品名称（下单时快照）
     */
    private String productName;

    /**
     * 商品主图（下单时快照）
     */
    private String productImage;

    /**
     * 下单时单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 小计（price * quantity）
     */
    private BigDecimal totalPrice;

    /**
     * 下单时的规格快照（如{"颜色":"黑色","尺寸":"M"}）
     */
    private String specInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
