package com.yuexuan.mall.common;

import lombok.Getter;

/**
 * 业务异常（可携带自定义错误码）
 */
@Getter
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
}
