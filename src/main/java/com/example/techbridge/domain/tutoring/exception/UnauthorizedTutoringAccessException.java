package com.example.techbridge.domain.tutoring.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class UnauthorizedTutoringAccessException extends BusinessException {

    public UnauthorizedTutoringAccessException() {
        super(ErrorCode.UNAUTHORIZED_TUTORING_ACCESS);
    }
}
