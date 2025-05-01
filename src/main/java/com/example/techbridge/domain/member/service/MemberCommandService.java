package com.example.techbridge.domain.member.service;

import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.exception.EmailAlreadyExistsException;
import com.example.techbridge.domain.member.exception.InvalidMemberPasswordException;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.exception.UsernameAlreadyExistsException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final StudentService studentService;
    private final TutorService tutorService;
    private final PasswordEncoder passwordEncoder;

    private Map<Role, BiConsumer<Member, SignUpRequestWrapper>> postProcessors;
    private static final BiConsumer<Member, SignUpRequestWrapper> NOOP = (m, w) -> {
    };

    @PostConstruct
    private void initPostProcessors() {
        postProcessors = Map.of(
            Role.STUDENT, (m, w) -> studentService.saveStudentInfo(m, w.getStudent()),
            Role.TUTOR, (m, w) -> tutorService.saveTutorInfo(m, w.getTutor())
        );
    }

    @Transactional
    public Member signUp(SignUpRequestWrapper wrapper) {
        SignUpRequest request = wrapper.getMember();

        validateDuplicateUsername(request.getUsername());
        validateDuplicateEmail(request.getEmail());

        Member member = Member.of(request, passwordEncoder::encode);
        Member saved = memberRepository.save(member);

        if (saved.getRole() == Role.STUDENT && wrapper.getStudent() == null) {
            throw new IllegalArgumentException("학생 추가 정보가 필요합니다.");
        }

        if (saved.getRole() == Role.TUTOR && wrapper.getTutor() == null) {
            throw new IllegalArgumentException("튜터 추가 정보가 필요합니다.");
        }

        postProcessors.getOrDefault(saved.getRole(), NOOP)
            .accept(saved, wrapper);

        return saved;
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