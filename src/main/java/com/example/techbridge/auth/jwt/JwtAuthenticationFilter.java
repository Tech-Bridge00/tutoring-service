package com.example.techbridge.auth.jwt;

import com.example.techbridge.auth.dto.LoginMember;
import com.example.techbridge.auth.service.TokenBlacklistService;
import com.example.techbridge.domain.member.entity.Member.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final List<String> whiteList;
    private final TokenBlacklistService blacklistService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
        TokenBlacklistService blacklistService,
        List<String> whiteList) {
        this.tokenProvider = tokenProvider;
        this.blacklistService = blacklistService;
        this.whiteList = whiteList;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {
        String token;
        try {
            token = tokenProvider.resolveBearer(request);
        } catch (IllegalArgumentException ex) {
            log.warn("잘못된 Authorization 헤더 형식: {}", ex.getMessage());
            response.sendError((HttpStatus.BAD_REQUEST.value()), "잘못된 토큰 형식입니다.");
            return;
        }

        if (token != null && tokenProvider.isValid(token)) {
            String jti = tokenProvider.getJti(token);
            if (blacklistService.isBlacklisted(jti)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "토큰이 취소되었습니다.");
                return;
            }

            Long memberId = tokenProvider.extractMemberId(token);
            Role role = tokenProvider.extractRole(token);
            LoginMember loginMember = new LoginMember(memberId, role);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    loginMember,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));

            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}