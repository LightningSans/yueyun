package com.yuexuan.mall.common;

/**
 * 未授权异常（401）
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
