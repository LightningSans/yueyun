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
 * 支付记录表（追加写入，无操作人字段）
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("payment")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号（冗余）
     */
    private String orderNo;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式：BALANCE-余额支付，MOCK_PAY-模拟支付
     */
    private String paymentMethod;

    /**
     * 1-支付成功 0-失败/待支付
     */
    private Integer payStatus;

    /**
     * 备注（记录失败原因等）
     */
    private String remark;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;


}
