package com.example.techbridge.domain.member.service;

import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.exception.EmailAlreadyExistsException;
import com.example.techbridge.domain.member.exception.InvalidMemberPasswordException;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.exception.UsernameAlreadyExistsException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member signUp(SignUpRequest request) {
        validateDuplicateUsername(request.getUsername());
        validateDuplicateEmail(request.getEmail());

        Member member = request.toEntity();
        member.encodePassword(passwordEncoder.encode(request.getPassword()));

        return memberRepository.save(member);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(MemberNotFoundException::new);

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new InvalidMemberPasswordException();
        }

        member.encodePassword(passwordEncoder.encode(newPassword));
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

    private void validateDuplicateUsername(String username) {
        if (memberRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException();
        }
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }
    }
}
