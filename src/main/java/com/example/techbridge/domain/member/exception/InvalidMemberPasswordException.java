package com.example.techbridge.domain.member.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class InvalidMemberPasswordException extends BusinessException {

    public InvalidMemberPasswordException() {
        super(ErrorCode.INVALID_MEMBER_PASSWORD);
    }
}
