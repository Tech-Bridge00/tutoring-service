package com.example.techbridge.domain.tutoring.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class InvalidTutoringStatusException extends BusinessException {

    public InvalidTutoringStatusException() {
        super(ErrorCode.INVALID_TUTORING_STATUS);
    }
}
