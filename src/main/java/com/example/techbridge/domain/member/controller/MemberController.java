package com.example.techbridge.domain.member.controller;

import com.example.techbridge.domain.member.dto.MemberResponse;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.service.MemberService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> signUp(@Valid @RequestBody SignUpRequestWrapper request) {
        Member savedMember = memberService.signUp(request);
        URI location = URI.create("/api/members/" + savedMember.getId());
        return ResponseEntity.created(location)
            .body(new MemberResponse(savedMember));
    }

    @GetMapping("/{id}")
    public MemberResponse findById(@PathVariable Long id) {
        return new MemberResponse(memberService.findById(id));
    }

    @GetMapping
    public MemberResponse findByUsernameAndEmail(@RequestParam(required = false) String username,
        @RequestParam(required = false) String email) {

        if (username == null && email == null) {
            throw new IllegalArgumentException("username, email 중 하나는 필수 입력 사항입니다.");
        }

        Member m = (username != null)
            ? memberService.findByUsername(username)
            : memberService.findByEmail(email);

        return new MemberResponse(m);
    }

    @RequestMapping(method = RequestMethod.HEAD)
    public ResponseEntity<Void> exists(@RequestParam(required = false) String username,
        @RequestParam(required = false) String email) {

        if (username == null && email == null) {
            throw new IllegalArgumentException("username, email 중 하나는 필수 입력 사항입니다.");
        }

        boolean exists = (username != null) ? memberService.existsByUsername(username)
            : memberService.existsByEmail(email);

        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
