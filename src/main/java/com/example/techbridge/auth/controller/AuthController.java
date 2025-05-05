package com.example.techbridge.auth.controller;

import com.example.techbridge.auth.dto.LoginRequest;
import com.example.techbridge.auth.dto.RefreshRequest;
import com.example.techbridge.auth.dto.TokenResponse;
import com.example.techbridge.auth.exception.InvalidTokenException;
import com.example.techbridge.auth.exception.RefreshTokenNotFoundException;
import com.example.techbridge.auth.jwt.JwtTokenProvider;
import com.example.techbridge.auth.repository.RefreshTokenRepository;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.exception.InvalidMemberPasswordException;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.example.techbridge.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public CommonResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        Member member = memberRepository.findByUsername(request.getUsername())
            .orElseThrow(MemberNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidMemberPasswordException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getId(),
            member.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId(),
            member.getRole().name());

        refreshTokenRepository.save(member.getId(), refreshToken);

        return CommonResponse.success(new TokenResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public CommonResponse<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        if (!jwtTokenProvider.isValid(request.getRefreshToken())) {
            throw new InvalidTokenException();
        }

        Long memberId = jwtTokenProvider.extractMemberId(request.getRefreshToken());
        String savedToken = refreshTokenRepository.findByMemberId(memberId)
            .orElseThrow(RefreshTokenNotFoundException::new);

        if (!savedToken.equals(request.getRefreshToken())) {
            throw new InvalidTokenException();
        }

        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        String newAccessToken = jwtTokenProvider.generateAccessToken(member.getId(),
            member.getRole().toString());

        return CommonResponse.success(new TokenResponse(newAccessToken, request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public CommonResponse<Void> logout(@RequestBody RefreshRequest request) {
        if (!jwtTokenProvider.isValid(request.getRefreshToken())) {
            throw new InvalidTokenException();
        }

        Long memberId = jwtTokenProvider.extractMemberId(request.getRefreshToken());
        String savedToken = refreshTokenRepository.findByMemberId(memberId)
            .orElseThrow(RefreshTokenNotFoundException::new);

        if (!savedToken.equals(request.getRefreshToken())) {
            throw new InvalidTokenException();
        }

        refreshTokenRepository.delete(memberId);

        return CommonResponse.success(null);
    }
}
