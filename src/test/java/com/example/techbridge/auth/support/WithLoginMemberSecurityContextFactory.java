package com.example.techbridge.auth.support;

import com.example.techbridge.auth.dto.LoginMember;
import com.example.techbridge.domain.member.entity.Member.Role;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithLoginMemberSecurityContextFactory implements
    WithSecurityContextFactory<WithLoginMember> {

    @Override
    public SecurityContext createSecurityContext(WithLoginMember annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        LoginMember principal = new LoginMember(annotation.id(), Role.valueOf(annotation.role()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "",
            null);

        context.setAuthentication(authentication);
        return context;
    }
}