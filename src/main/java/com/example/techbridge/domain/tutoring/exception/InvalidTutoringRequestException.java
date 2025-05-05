package com.example.techbridge.domain.tutoring.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class InvalidTutoringRequestException extends BusinessException {

    public InvalidTutoringRequestException() {
        super(ErrorCode.INVALID_TUTORING_REQUEST);
    }
}
