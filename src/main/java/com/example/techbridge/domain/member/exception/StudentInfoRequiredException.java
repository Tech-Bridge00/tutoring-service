package com.example.techbridge.domain.member.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class StudentInfoRequiredException extends BusinessException {

    public StudentInfoRequiredException() {
        super(ErrorCode.STUDENT_INFO_REQUIRED);
    }
}
