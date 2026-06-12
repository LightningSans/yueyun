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
 * 收货地址表
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("address")
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 省名称
     */
    private String provinceName;

    /**
     * 市名称
     */
    private String cityName;

    /**
     * 区/县名称
     */
    private String districtName;

    /**
     * 详细地址（街道、楼号、门牌）
     */
    private String detailAddress;

    /**
     * 邮编
     */
    private String postalCode;

    /**
     * 是否默认地址：1-是，0-否
     */
    private Integer isDefault;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer isDeleted;

    /**
     * 创建人ID（关联 user.id）
     */
    private Long createBy;

    /**
     * 最后操作人ID（关联 user.id）
     */
    private Long updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


}
