package com.example.techbridge.domain.member.service;

import com.example.techbridge.domain.member.dto.MemberDetailResponse;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    public MemberDetailResponse findById(Long id) {
        Member member = memberRepository.findWithDetailsById(id)
            .orElseThrow(MemberNotFoundException::new);

        if (member.getRole() == Role.STUDENT && member.getStudent() == null ||
            member.getRole() == Role.TUTOR && member.getTutor() == null) {
            throw new MemberNotFoundException();
        }

        String profileImageUrl = null;
        if (member.getProfileImageKey() != null) {
            profileImageUrl = s3Uploader.generateViewUrl(member.getProfileImageKey(),
                Duration.ofMinutes(5)).toString();
        }

        return MemberDetailResponse.of(member, profileImageUrl);
    }

    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
            .orElseThrow(MemberNotFoundException::new);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
            .orElseThrow(MemberNotFoundException::new);
    }

    public boolean existsByUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public Page<MemberDetailResponse> findAll(Role role, Pageable pageable) {
        Page<Member> page = (role == null) ? memberRepository.findAll(pageable)
            : memberRepository.findByRole(role, pageable);

        return page.map(member -> {
            String profileImageUrl = null;
            if (member.getProfileImageKey() != null) {
                profileImageUrl = s3Uploader.generateViewUrl(member.getProfileImageKey(),
                    Duration.ofMinutes(5)).toString();
            }

            return MemberDetailResponse.of(member, profileImageUrl);
        });
    }
}
