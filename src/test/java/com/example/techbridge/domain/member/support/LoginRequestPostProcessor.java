package com.example.techbridge.domain.member.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import com.example.techbridge.auth.dto.LoginMember;
import com.example.techbridge.domain.member.entity.Member;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public class LoginRequestPostProcessor {

    public static RequestPostProcessor loginMember(Member member) {
        LoginMember principal = new LoginMember(member.getId(), member.getRole());
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + member.getRole().name())
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        return authentication(auth);
    }
}
