package com.yuexuan.mall.entity.vo;

import com.yuexuan.mall.entity.po.Review;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评价管理视图对象 — 扩展商品名称、图片和用户昵称
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReviewVO extends Review {

    /** 商品名称 */
    private String productName;

    /** 商品主图 */
    private String productImage;

    /** 用户昵称 */
    private String userNickname;
}
