package com.example.techbridge.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ====== MEMBER ======
    MEMBER_NOT_FOUND("M001", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("M002", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS("M003", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    INVALID_MEMBER_PASSWORD("M004", "비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),

    // ====== COMMON ======
    BAD_REQUEST("C400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("C405", "허용되지 않은 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    INTERNAL_SERVER_ERROR("C999", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

}
