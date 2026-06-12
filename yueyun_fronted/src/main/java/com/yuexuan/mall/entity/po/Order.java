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
 * 订单表
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号（唯一，对外展示）
     */
    private String orderNo;

    /**
     * 买家ID
     */
    private Long userId;

    /**
     * 商品总金额（未减优惠）
     */
    private BigDecimal totalAmount;

    /**
     * 优惠金额（默认0）
     */
    private BigDecimal discountAmount;

    /**
     * 运费（默认3）
     */
    private BigDecimal freightAmount;

    /**
     * 实付金额（total - discount + freight）
     */
    private BigDecimal payAmount;

    /**
     * 支付方式：BALANCE-余额支付，MOCK_PAY-模拟支付
     */
    private String paymentMethod;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 配送员ID（初始为空）
     */
    private Long courierId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 省
     */
    private String receiverProvince;

    /**
     * 市
     */
    private String receiverCity;

    /**
     * 区/县
     */
    private String receiverDistrict;

    /**
     * 详细地址
     */
    private String receiverDetail;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 创建人ID（关联 user.id，买家自己下单）
     */
    private Long createBy;

    /**
     * 最后操作人ID（关联 admin.id 或 courier.id）
     */
    private Long updateBy;

    /**
     * 下单时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer isDeleted;


}
