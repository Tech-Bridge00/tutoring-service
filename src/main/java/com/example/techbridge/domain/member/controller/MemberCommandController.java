package com.example.techbridge.domain.member.controller;

import com.example.techbridge.domain.member.dto.MemberDetailResponse;
import com.example.techbridge.domain.member.dto.MemberUpdateWrapper;
import com.example.techbridge.domain.member.dto.PasswordChangeRequest;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.service.MemberCommandService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberCommandController {

    private final MemberCommandService memberCommandService;

    @PostMapping
    public ResponseEntity<MemberDetailResponse> signUp(@Valid @RequestBody SignUpRequestWrapper request) {
        Member savedMember = memberCommandService.signUp(request);
        URI location = URI.create("/api/members/" + savedMember.getId());
        return ResponseEntity.created(location)
            .body(MemberDetailResponse.of(savedMember));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
        @Valid @RequestBody PasswordChangeRequest request) {

        memberCommandService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public MemberDetailResponse updateMemberInfo(@PathVariable Long id,
        @Valid @RequestBody MemberUpdateWrapper request,
        @AuthenticationPrincipal Member loginMember) {
        Member updatedMember = memberCommandService.updateMember(id, request, loginMember.getId());
        return MemberDetailResponse.of(updatedMember);
    }
}
