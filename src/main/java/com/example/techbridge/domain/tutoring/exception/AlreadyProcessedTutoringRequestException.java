package com.example.techbridge.domain.tutoring.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class AlreadyProcessedTutoringRequestException extends BusinessException {

    public AlreadyProcessedTutoringRequestException() {
        super(ErrorCode.ALREADY_PROCESSED_TUTORING_REQUEST);
    }
}
