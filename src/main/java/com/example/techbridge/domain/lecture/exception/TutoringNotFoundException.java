package com.example.techbridge.domain.lecture.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class TutoringNotFoundException extends BusinessException {

    public TutoringNotFoundException() {
        super(ErrorCode.TUTORING_NOT_FOUND);
    }
}
