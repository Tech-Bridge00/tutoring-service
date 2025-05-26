package com.example.techbridge.global.common;

import com.example.techbridge.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;

@Getter
public class CommonResponse<T> {

    private final String code;
    private final String message;

    @JsonInclude(Include.NON_NULL)
    private T data;

    private CommonResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private CommonResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", null);
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> CommonResponse<T> error(ErrorCode errorCode) {
        return new CommonResponse<>(errorCode.getCode(), errorCode.getMessage());
    }
}
