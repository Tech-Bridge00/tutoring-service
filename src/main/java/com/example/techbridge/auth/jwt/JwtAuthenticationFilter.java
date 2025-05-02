package com.example.techbridge.auth.jwt;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.repository.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final List<String> whiteList;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
        MemberRepository memberRepository,
        List<String> whiteList) {
        this.tokenProvider = tokenProvider;
        this.memberRepository = memberRepository;
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

                Member member = memberRepository.findById(memberId)
                    .orElseThrow(MemberNotFoundException::new);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        member,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));

                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}