package com.example.techbridge.domain.tutoring.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class TutoringAlreadyExistsException extends BusinessException {

    public TutoringAlreadyExistsException() {
        super(ErrorCode.TUTORING_ALREADY_EXISTS);
    }
}
