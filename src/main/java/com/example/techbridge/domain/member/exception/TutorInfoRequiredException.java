package com.example.techbridge.domain.member.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class TutorInfoRequiredException extends BusinessException {

    public TutorInfoRequiredException() {
        super(ErrorCode.TUTOR_INFO_REQUEST);
    }
}
