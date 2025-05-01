package com.example.techbridge.domain.member.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class SameAsOldPasswordException extends BusinessException {

    public SameAsOldPasswordException() {
        super(ErrorCode.SAME_AS_OLD_PASSWORD);
    }
}
