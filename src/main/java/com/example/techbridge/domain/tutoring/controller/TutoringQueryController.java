package com.example.techbridge.domain.tutoring.controller;

import com.example.techbridge.auth.dto.LoginMember;
import com.example.techbridge.domain.tutoring.dto.ReceiveTutoringSimpleResponse;
import com.example.techbridge.domain.tutoring.dto.RequestTutoringSimpleResponse;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import com.example.techbridge.domain.tutoring.service.TutoringQueryService;
import com.example.techbridge.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutoring")
@RequiredArgsConstructor
public class TutoringQueryController {

    private final TutoringQueryService tutoringQueryService;

    // 과외 신청한 목록 조회
    @GetMapping("/sent")
    public CommonResponse<Page<RequestTutoringSimpleResponse>> getSentTutoringList(
        @RequestParam(required = false) RequestStatus status,
        @AuthenticationPrincipal LoginMember loginMember,
        Pageable pageable
    ) {
        Page<RequestTutoringSimpleResponse> result =
            tutoringQueryService.getSentTutoringList(loginMember.getId(), status, pageable);
        return CommonResponse.success(result);
    }

    // 과외 신청 받은 목록 조회
    @GetMapping("/received")
    public CommonResponse<Page<ReceiveTutoringSimpleResponse>> getReceivedTutoringList(
        @RequestParam(required = false) RequestStatus status,
        @AuthenticationPrincipal LoginMember loginMember,
        Pageable pageable
    ) {
        Page<ReceiveTutoringSimpleResponse> result =
            tutoringQueryService.getReceivedTutoringList(loginMember.getId(), status, pageable);
        return CommonResponse.success(result);
    }
}
