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
    SAME_AS_OLD_PASSWORD("M005", "기존 비밀번호와 동일합니다.", HttpStatus.BAD_REQUEST),
    STUDENT_INFO_REQUIRED("M006", "학생 추가 정보가 필요합니다.", HttpStatus.BAD_REQUEST),
    TUTOR_INFO_REQUEST("M007", "튜터 추가 정보가 필요합니다.", HttpStatus.BAD_REQUEST),
    INVALID_MEMBER_QUERY("M008", "username 또는 email 중 하나는 반드시 필요합니다.", HttpStatus.BAD_REQUEST),

    // ====== TUTORING ======
    INVALID_TUTORING_REQUEST("T001", "자기 자신에게는 과외를 신청할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TUTORING_TIME("T002", "과외 시간 정보가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    TUTORING_ALREADY_EXISTS("T003", "해당 시간에 이미 수락된 과외가 존재합니다.", HttpStatus.BAD_REQUEST),
    TUTORING_NOT_FOUND("T004", "존재하지 않는 과외입니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_TUTORING_ACCESS("T005", "과외 수신자 정보가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ALREADY_PROCESSED_TUTORING_REQUEST("T006", "과외 요청 상태가 이미 처리되었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TUTORING_STATUS("T007", "취소할 수 없는 상태입니다.", HttpStatus.BAD_REQUEST),
    INVALID_TUTORING_REQUEST_TYPE("T008", "지원하지 않는 조회 타입입니다.", HttpStatus.BAD_REQUEST),

    // ====== AUTH ======
    INVALID_TOKEN("A001", "유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_FOUND("A002", "Refresh Token이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("A003", "접근 권한이 필요합니다.", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED("A004", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),

    // ====== COMMON ======
    BAD_REQUEST("C400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("C405", "허용되지 않은 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    INTERNAL_SERVER_ERROR("C999", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;

}
