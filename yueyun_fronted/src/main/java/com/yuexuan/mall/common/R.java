package com.yuexuan.mall.common;

import lombok.Data;

/**
 * 统一响应体 R<T>
 * 所有 API 接口统一返回此格式
 */
@Data
public class R<T> {

    private int code;
    private String msg;
    private T data;

    private R() {}

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ─── 成功响应 ───
    public static <T> R<T> success() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> R<T> success(String msg) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, null);
    }

    public static <T> R<T> success(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> R<T> success(String msg, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    // ─── 失败响应 ───
    public static <T> R<T> failed() {
        return new R<>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage(), null);
    }

    public static <T> R<T> failed(String msg) {
        return new R<>(ResultCode.FAILED.getCode(), msg, null);
    }

    public static <T> R<T> failed(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> R<T> failed(ResultCode resultCode, String msg) {
        return new R<>(resultCode.getCode(), msg, null);
    }

    public static <T> R<T> failed(int code, String msg) {
        return new R<>(code, msg, null);
    }

    // ─── 便捷静态方法 ───
    public static <T> R<T> unauthorized(String msg) {
        return new R<>(ResultCode.UNAUTHORIZED.getCode(), msg, null);
    }

    public static <T> R<T> forbidden(String msg) {
        return new R<>(ResultCode.FORBIDDEN.getCode(), msg, null);
    }

    public static <T> R<T> validateFailed(String msg) {
        return new R<>(ResultCode.VALIDATE_FAILED.getCode(), msg, null);
    }

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }
}
