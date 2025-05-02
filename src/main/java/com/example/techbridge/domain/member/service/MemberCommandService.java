package com.example.techbridge.domain.member.service;

import com.example.techbridge.domain.member.dto.MemberUpdateWrapper;
import com.example.techbridge.domain.member.dto.PasswordChangeRequest;
import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.exception.EmailAlreadyExistsException;
import com.example.techbridge.domain.member.exception.InvalidMemberPasswordException;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.exception.SameAsOldPasswordException;
import com.example.techbridge.domain.member.exception.StudentInfoRequiredException;
import com.example.techbridge.domain.member.exception.TutorInfoRequiredException;
import com.example.techbridge.domain.member.exception.UnauthorizedException;
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
            throw new StudentInfoRequiredException();
        }

        if (saved.getRole() == Role.TUTOR && wrapper.getTutor() == null) {
            throw new TutorInfoRequiredException();
        }

        postProcessors.getOrDefault(saved.getRole(), NOOP)
            .accept(saved, wrapper);

        return saved;
    }

    @Transactional
    public void changePassword(Long id, PasswordChangeRequest request) {
        Member member = memberRepository.findById(id)
            .orElseThrow(MemberNotFoundException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new InvalidMemberPasswordException();
        }

        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw new SameAsOldPasswordException();
        }

        member.encodePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public Member updateMember(Long id, MemberUpdateWrapper request, Long loginMemberId) {
        validateSameMember(id, loginMemberId);

        Member foundMember = memberRepository.findById(id)
            .orElseThrow(MemberNotFoundException::new);

        foundMember.updateProfile(request.getMember());

        updateStudentInfo(request, foundMember);

        updateTutorInfo(request, foundMember);

        return foundMember;
    }

    private void updateTutorInfo(MemberUpdateWrapper request, Member foundMember) {
        if (foundMember.getRole() == Role.TUTOR) {
            if (request.getTutor() == null) {
                throw new TutorInfoRequiredException();
            }

            tutorService.updateTutorInfo(foundMember, request.getTutor());
        }
    }

    private void updateStudentInfo(MemberUpdateWrapper request, Member foundMember) {
        if (foundMember.getRole() == Role.STUDENT) {
            if (request.getStudent() == null) {
                throw new StudentInfoRequiredException();
            }

            studentService.updateStudentInfo(foundMember, request.getStudent());
        }
    }

    private void validateSameMember(Long id, Long loginMemberId) {
        if (!id.equals(loginMemberId)) {
            throw new UnauthorizedException();
        }
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