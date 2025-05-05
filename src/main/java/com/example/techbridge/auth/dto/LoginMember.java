package com.example.techbridge.auth.dto;

import com.example.techbridge.domain.member.entity.Member.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginMember {

    private final Long id;
    private final Role role;
}
