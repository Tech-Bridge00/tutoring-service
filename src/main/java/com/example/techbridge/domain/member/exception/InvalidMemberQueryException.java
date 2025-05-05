package com.example.techbridge.domain.member.exception;

import com.example.techbridge.global.exception.BusinessException;
import com.example.techbridge.global.exception.ErrorCode;

public class InvalidMemberQueryException extends BusinessException {

    public InvalidMemberQueryException() {
        super(ErrorCode.INVALID_MEMBER_QUERY);
    }
}
