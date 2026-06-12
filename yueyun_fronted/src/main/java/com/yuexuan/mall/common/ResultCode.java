package com.yuexuan.mall.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "成功"),
    FAILED(400, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    USER_DISABLED(4001, "账户已被禁用"),
    USER_NOT_FOUND(4002, "用户不存在"),
    PASSWORD_ERROR(4003, "密码错误"),
    USERNAME_EXISTS(4004, "用户名已存在"),
    STOCK_NOT_ENOUGH(4005, "库存不足"),
    ORDER_STATUS_ERROR(4006, "订单状态异常"),
    PAYMENT_FAILED(4007, "支付失败"),
    PRODUCT_OFFLINE(4008, "商品已下架"),

    INTERNAL_ERROR(5000, "服务器内部错误");

    private final int code;
    private final String message;
}
