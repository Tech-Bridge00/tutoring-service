package com.example.techbridge.domain.tutoring.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class InvalidTutoringRequestTypeException extends BusinessException {

    public InvalidTutoringRequestTypeException() {
        super(ErrorCode.INVALID_TUTORING_REQUEST_TYPE);
    }
}
