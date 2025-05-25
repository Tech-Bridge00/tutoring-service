package com.example.techbridge.domain.member.controller;

import com.example.techbridge.auth.dto.LoginMember;
import com.example.techbridge.auth.jwt.JwtTokenProvider;
import com.example.techbridge.auth.service.RefreshTokenService;
import com.example.techbridge.auth.service.TokenBlacklistService;
import com.example.techbridge.domain.member.dto.MemberDetailResponse;
import com.example.techbridge.domain.member.dto.MemberUpdateWrapper;
import com.example.techbridge.domain.member.dto.PasswordChangeRequest;
import com.example.techbridge.domain.member.dto.PreSignedUrlResponse;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.service.MemberCommandService;
import com.example.techbridge.domain.member.service.S3Uploader;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberCommandController {

    private final MemberCommandService memberCommandService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService blacklistService;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Uploader s3Uploader;

    @PostMapping
    public ResponseEntity<MemberDetailResponse> signUp(
        @Valid @RequestBody SignUpRequestWrapper request) {
        Member savedMember = memberCommandService.signUp(request);
        URI location = URI.create("/api/members/" + savedMember.getId());
        return ResponseEntity.created(location)
            .body(MemberDetailResponse.of(savedMember));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
        @Valid @RequestBody PasswordChangeRequest request,
        @AuthenticationPrincipal LoginMember loginMember) {

        memberCommandService.changePassword(id, request, loginMember.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public MemberDetailResponse updateMemberInfo(@PathVariable Long id,
        @Valid @RequestBody MemberUpdateWrapper request,
        @AuthenticationPrincipal LoginMember loginMember) {
        Member updatedMember = memberCommandService.updateMember(id, request, loginMember.getId());
        return MemberDetailResponse.of(updatedMember);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id,
        @AuthenticationPrincipal LoginMember loginMember,
        HttpServletRequest request) {
        memberCommandService.deleteMember(id, loginMember.getId());

        refreshTokenService.deleteByMemberId(loginMember.getId());

        String token = jwtTokenProvider.resolveBearer(request);
        if (token != null) {
            String jti = jwtTokenProvider.getJti(token);
            Duration ttl = jwtTokenProvider.getRemainingTTL(token);
            blacklistService.blacklist(jti, ttl);
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile-image-url")
    public PreSignedUrlResponse getUploadUrl(@RequestParam String filename,
        @RequestParam String contentType) {
        String key = "profile-images/" + System.currentTimeMillis() + "-" + UUID.randomUUID() + "-" + filename;
        URL url = s3Uploader.generateUploadUrl(key, contentType, Duration.ofMinutes(5));
        return new PreSignedUrlResponse(url.toString(), key);
    }
}
