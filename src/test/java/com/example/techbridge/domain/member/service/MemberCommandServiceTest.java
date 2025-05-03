package com.example.techbridge.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.techbridge.domain.member.dto.MemberUpdateRequest;
import com.example.techbridge.domain.member.dto.MemberUpdateWrapper;
import com.example.techbridge.domain.member.dto.PasswordChangeRequest;
import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.dto.StudentInfoRequest;
import com.example.techbridge.domain.member.dto.StudentUpdateRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Student;
import com.example.techbridge.domain.member.entity.Tutor;
import com.example.techbridge.domain.member.exception.EmailAlreadyExistsException;
import com.example.techbridge.domain.member.exception.InvalidMemberPasswordException;
import com.example.techbridge.domain.member.exception.SameAsOldPasswordException;
import com.example.techbridge.domain.member.exception.StudentInfoRequiredException;
import com.example.techbridge.domain.member.exception.TutorInfoRequiredException;
import com.example.techbridge.domain.member.exception.UnauthorizedException;
import com.example.techbridge.domain.member.exception.UsernameAlreadyExistsException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.example.techbridge.domain.member.repository.StudentRepository;
import com.example.techbridge.domain.member.repository.TutorRepository;
import com.example.techbridge.domain.member.support.AbstractMemberTestSupport;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberCommandServiceTest extends AbstractMemberTestSupport {

    @Autowired
    private MemberCommandService memberCommandService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TutorRepository tutorRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("STUDENT 가입 성공")
    void signUp_student_success() {
        Member saved = memberCommandService.signUp(initStudent());
        Optional<Student> student = studentRepository.findByMemberId(saved.getId());
        assertThat(student).isPresent();
        assertThat(tutorRepository.findByMemberId(saved.getId())).isEmpty();
        assertThat(passwordEncoder.matches("test1234", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("TUTOR 가입 성공")
    void signUp_tutor_success() {
        Member saved = memberCommandService.signUp(initTutor());
        Optional<Tutor> tutor = tutorRepository.findByMemberId(saved.getId());
        assertThat(tutor).isPresent();
        assertThat(studentRepository.findByMemberId(saved.getId())).isEmpty();
        assertThat(passwordEncoder.matches("test1234", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("STUDENT 추가정보 누락 예외")
    void signUp_student_missingInfo_fail() {
        SignUpRequestWrapper wrapper = SignUpRequestWrapper.builder()
            .member(initStudent().getMember())
            .build();
        assertThatThrownBy(() -> memberCommandService.signUp(wrapper))
            .isInstanceOf(StudentInfoRequiredException.class);
    }

    @Test
    @DisplayName("TUTOR 추가정보 누락 예외")
    void signUp_tutor_missingInfo_fail() {
        SignUpRequestWrapper wrapper = SignUpRequestWrapper.builder()
            .member(initTutor().getMember())
            .build();
        assertThatThrownBy(() -> memberCommandService.signUp(wrapper))
            .isInstanceOf(TutorInfoRequiredException.class);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        Member saved = memberCommandService.signUp(initStudent());
        Long id = saved.getId();
        memberCommandService.changePassword(id, new PasswordChangeRequest("test1234", "newpass"));
        Member changed = memberRepository.findById(id).orElseThrow();
        assertThat(passwordEncoder.matches("newpass", changed.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 틀림")
    void changePassword_invalidCurrent_fail() {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        Long id = saved.getId();

        PasswordChangeRequest request = new PasswordChangeRequest("wrong_password", "new_password");

        // when, then
        assertThatThrownBy(() -> memberCommandService.changePassword(id, request))
            .isInstanceOf(InvalidMemberPasswordException.class);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 동일한 비밀번호")
    void changePassword_fail() {
        Member saved = memberCommandService.signUp(initStudent());
        Long id = saved.getId();
        PasswordChangeRequest request = new PasswordChangeRequest("test1234", "test1234");
        assertThatThrownBy(() -> memberCommandService.changePassword(id, request))
            .isInstanceOf(SameAsOldPasswordException.class);
    }

    @Test
    @DisplayName("username/email 중복 체크")
    void duplicate_username_or_email_fail() {
        // given
        SignUpRequestWrapper wrapper = initStudent();
        memberCommandService.signUp(wrapper);

        StudentInfoRequest student = wrapper.getStudent();
        SignUpRequest duplicatedUsername = wrapper.getMember().toBuilder()
            .email(uuid() + "@email.com")
            .build();

        SignUpRequestWrapper usernameConflict = SignUpRequestWrapper.builder()
            .member(duplicatedUsername)
            .student(student)
            .build();

        SignUpRequest duplicatedEmail = wrapper.getMember().toBuilder()
            .username(uuid())
            .build();

        SignUpRequestWrapper emailConflict = SignUpRequestWrapper.builder()
            .member(duplicatedEmail)
            .student(student)
            .build();

        // when, then
        assertThatThrownBy(() -> memberCommandService.signUp(usernameConflict))
            .isInstanceOf(UsernameAlreadyExistsException.class);

        assertThatThrownBy(() -> memberCommandService.signUp(emailConflict))
            .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void update_member_success() {
        Member saved = memberCommandService.signUp(initStudent());
        Long id = saved.getId();

        MemberUpdateWrapper updateRequest = MemberUpdateWrapper.builder()
            .member(MemberUpdateRequest.builder()
                .nickname("변경닉")
                .contact("01012341234")
                .location("서울 중랑구")
                .build())
            .student(StudentUpdateRequest.builder()
                .interestedField("AI")
                .status("졸업예정")
                .build())
            .build();

        Member updated = memberCommandService.updateMember(id, updateRequest, id);
        assertThat(updated.getNickname()).isEqualTo("변경닉");
        assertThat(updated.getStudent().getInterestedField()).isEqualTo("AI");
    }

    @Test
    @DisplayName("다른 사용자로 수정 시도하면 예외 발생")
    void update_other_user_fail() {
        Member saved = memberCommandService.signUp(initStudent());
        Long id = saved.getId();

        MemberUpdateWrapper updateRequest = MemberUpdateWrapper.builder()
            .member(MemberUpdateRequest.builder().nickname("불가").build())
            .student(StudentUpdateRequest.builder().interestedField("X").status("X").build())
            .build();

        assertThatThrownBy(() -> memberCommandService.updateMember(id, updateRequest, id + 1))
            .isInstanceOf(UnauthorizedException.class);
    }
}
