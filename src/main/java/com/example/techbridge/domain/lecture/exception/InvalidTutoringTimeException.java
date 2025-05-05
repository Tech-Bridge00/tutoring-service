package com.example.techbridge.domain.lecture.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class InvalidTutoringTimeException extends BusinessException {

    public InvalidTutoringTimeException() {
        super(ErrorCode.INVALID_TUTORING_TIME);
    }
}
