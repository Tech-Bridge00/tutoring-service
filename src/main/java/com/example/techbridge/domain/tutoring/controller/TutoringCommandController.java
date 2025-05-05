package com.example.techbridge.domain.tutoring.controller;

import com.example.techbridge.auth.dto.LoginMember;
import com.example.techbridge.domain.tutoring.dto.TutoringRequest;
import com.example.techbridge.domain.tutoring.service.TutoringCommandService;
import com.example.techbridge.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutoring")
@RequiredArgsConstructor
public class TutoringCommandController {

    private final TutoringCommandService tutoringCommandService;

    // 과외 신청
    @PostMapping
    public ResponseEntity<CommonResponse<Void>> requestTutoring(
        @RequestBody TutoringRequest request,
        @AuthenticationPrincipal LoginMember loginMember
    ) {
        tutoringCommandService.requestTutoring(request, loginMember.getId());
        return ResponseEntity.ok(CommonResponse.success());
    }

    // 과외 수락
    @PostMapping("/{tutoringId}/accept")
    public ResponseEntity<CommonResponse<Void>> acceptTutoring(
        @PathVariable Long tutoringId,
        @AuthenticationPrincipal LoginMember loginMember
    ) {
        tutoringCommandService.acceptTutoring(tutoringId, loginMember.getId());
        return ResponseEntity.ok(CommonResponse.success());
    }

    // 과외 거절
    @PostMapping("/{tutoringId}/reject")
    public ResponseEntity<CommonResponse<Void>> rejectTutoring(
        @PathVariable Long tutoringId,
        @AuthenticationPrincipal LoginMember loginMember
    ) {
        tutoringCommandService.rejectTutoring(tutoringId, loginMember.getId());
        return ResponseEntity.ok(CommonResponse.success());
    }

    // 과외 취소
    @PostMapping("/{tutoringId}/cancel")
    public ResponseEntity<CommonResponse<Void>> cancelTutoring(
        @PathVariable Long tutoringId,
        @AuthenticationPrincipal LoginMember loginMember
    ) {
        tutoringCommandService.cancelTutoring(tutoringId, loginMember.getId());
        return ResponseEntity.ok(CommonResponse.success());
    }
}
