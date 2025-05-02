package com.example.techbridge.auth.jwt;

import com.example.techbridge.auth.dto.LoginMember;
import com.example.techbridge.domain.member.entity.Member.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final List<String> whiteList;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
        List<String> whiteList) {
        this.tokenProvider = tokenProvider;
        this.whiteList = whiteList;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());

            if (tokenProvider.isValid(token)) {
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
        }

        filterChain.doFilter(request, response);
    }
}