package com.example.techbridge.domain.tutoring.controller;

import com.example.techbridge.auth.dto.LoginMember;
import com.example.techbridge.domain.tutoring.dto.TutoringListType;
import com.example.techbridge.domain.tutoring.dto.TutoringSimpleResponse;
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

    // 과외 목록 조회 (신청 or 수신)
    @GetMapping
    public CommonResponse<Page<? extends TutoringSimpleResponse>> getTutoringList(
        @RequestParam TutoringListType type,                          // SENT or RECEIVED
        @RequestParam(required = false) RequestStatus status,         // 필터용 status
        @AuthenticationPrincipal LoginMember loginMember,
        Pageable pageable
    ) {
        Page<? extends TutoringSimpleResponse> result =
            tutoringQueryService.getTutoringList(loginMember.getId(), type, status, pageable);
        return CommonResponse.success(result);
    }
}
