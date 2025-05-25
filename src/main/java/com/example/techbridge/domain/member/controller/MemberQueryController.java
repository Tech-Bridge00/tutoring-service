package com.example.techbridge.domain.member.controller;

import com.example.techbridge.domain.member.dto.MemberDetailResponse;
import com.example.techbridge.domain.member.dto.MemberResponse;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.exception.InvalidMemberQueryException;
import com.example.techbridge.domain.member.service.MemberQueryService;
import com.example.techbridge.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberQueryController {

    private final MemberQueryService memberQueryService;

    @GetMapping("/{id}")
    public MemberDetailResponse findById(@PathVariable Long id) {
        return memberQueryService.findById(id);
    }

    @GetMapping("/check")
    public MemberResponse findByUsernameAndEmail(@RequestParam(required = false) String username,
        @RequestParam(required = false) String email) {

        if (username == null && email == null) {
            throw new InvalidMemberQueryException();
        }

        Member m = (username != null)
            ? memberQueryService.findByUsername(username)
            : memberQueryService.findByEmail(email);

        return new MemberResponse(m);
    }

    @RequestMapping(path = "/check", method = RequestMethod.HEAD)
    public ResponseEntity<Void> exists(@RequestParam(required = false) String username,
        @RequestParam(required = false) String email) {

        if (username == null && email == null) {
            throw new InvalidMemberQueryException();
        }

        boolean exists = (username != null) ? memberQueryService.existsByUsername(username)
            : memberQueryService.existsByEmail(email);

        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping
    public PageResponse<MemberDetailResponse> findAll(
        @RequestParam(required = false) Role role,
        @PageableDefault(size = 10) Pageable pageable
    ) {

        Page<MemberDetailResponse> result = memberQueryService.findAll(role, pageable);
        return PageResponse.from(result);
    }
}
