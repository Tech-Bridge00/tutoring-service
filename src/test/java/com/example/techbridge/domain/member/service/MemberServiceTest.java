package com.example.techbridge.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.dto.StudentInfoRequest;
import com.example.techbridge.domain.member.dto.TutorInfoRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.entity.Student;
import com.example.techbridge.domain.member.entity.Tutor;
import com.example.techbridge.domain.member.exception.EmailAlreadyExistsException;
import com.example.techbridge.domain.member.exception.UsernameAlreadyExistsException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.example.techbridge.domain.member.repository.StudentRepository;
import com.example.techbridge.domain.member.repository.TutorRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String uuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String randomContact() {
        return "010" + ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);
    }

    private SignUpRequestWrapper initStudent() {
        String uuid = uuid();

        SignUpRequest memberInfo = SignUpRequest.builder()
            .username("student_" + uuid)
            .password("test1234")
            .name("홍길동")
            .nickname("학생_" + uuid)
            .age(21)
            .gender(Gender.M)
            .contact(randomContact())
            .email("student_" + uuid + "@email.com")
            .status("대학생")
            .role(Role.STUDENT)
            .location("서울 강남구 XXX")
            .build();

        StudentInfoRequest studentInfo = StudentInfoRequest.builder()
            .interestedField("백엔드")
            .status("재학중")
            .build();

        return SignUpRequestWrapper.builder()
            .member(memberInfo)
            .student(studentInfo)
            .build();
    }

    private SignUpRequestWrapper initTutor() {
        String uuid = uuid();

        SignUpRequest memberInfo = SignUpRequest.builder()
            .username("tutor_" + uuid)
            .password("test1234")
            .name("임꺽정")
            .nickname("튜터_" + uuid)
            .age(33)
            .gender(Gender.F)
            .contact(randomContact())
            .email("tutor_" + uuid + "@email.com")
            .status("직장인")
            .role(Role.TUTOR)
            .location("서울 송파구")
            .build();

        TutorInfoRequest tutorInfo = TutorInfoRequest.builder()
            .introduction("백엔드 튜터 테스트입니다.")
            .jobTitle("백엔드")
            .portfolioUrl("www.notion.so")
            .totalExperience(12)
            .currentlyEmployed(true)
            .build();

        return SignUpRequestWrapper.builder()
            .member(memberInfo)
            .tutor(tutorInfo)
            .build();
    }

    @Test
    @DisplayName("STUDENT 가입 성공")
    void signUp_student_success() {
        // when
        Member saved = memberService.signUp(initStudent());

        // then
        Optional<Student> findStudent = studentRepository.findByMemberId(saved.getId());
        assertThat(findStudent).isPresent();
        assertThat(tutorRepository.findByMemberId(saved.getId())).isEmpty();
        assertThat(passwordEncoder.matches("test1234", saved.getPassword())).isTrue();
        assertThat(findStudent.get().getInterestedField()).isEqualTo("백엔드");
    }

    @Test
    @DisplayName("TUTOR 가입 성공")
    void signUp_tutor_success() {
        // when
        Member saved = memberService.signUp(initTutor());

        // then
        Optional<Tutor> findTutor = tutorRepository.findByMemberId(saved.getId());
        assertThat(findTutor).isPresent();
        assertThat(studentRepository.findByMemberId(saved.getId())).isEmpty();
        assertThat(passwordEncoder.matches("test1234", saved.getPassword())).isTrue();
        assertThat(findTutor.get().getJobTitle()).isEqualTo("백엔드");
    }

    @Test
    @DisplayName("STUDENT 추가정보 누락 예외 발생")
    void signUp_student_missingInfo_fail() {
        SignUpRequestWrapper wrapper = SignUpRequestWrapper.builder()
            .member(initStudent().getMember())
            .build();

        assertThatThrownBy(() -> memberService.signUp(wrapper))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("추가 정보");
    }

    @Test
    @DisplayName("TUTOR 추가정보 누락 예외 발생")
    void signUp_tutor_missingInfo_fail() {
        SignUpRequestWrapper wrapper = SignUpRequestWrapper.builder()
            .member(initTutor().getMember())
            .build();

        assertThatThrownBy(() -> memberService.signUp(wrapper))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("추가 정보");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        SignUpRequestWrapper wrapper = initStudent();
        memberService.signUp(wrapper);

        // when
        memberService.changePassword(wrapper.getMember().getUsername(), "test1234",
            "newpassword1234");

        // then
        Member changed = memberRepository.findByUsername(wrapper.getMember().getUsername())
            .orElseThrow();
        assertThat(passwordEncoder.matches("newpassword1234", changed.getPassword())).isTrue();
    }

    @Test
    @DisplayName("username 중복 예외")
    void duplicate_username_fail() {
        // given
        SignUpRequestWrapper signedUp = initStudent();
        memberService.signUp(signedUp);

        // when
        SignUpRequest dupSignedUp = signedUp.getMember()
            .toBuilder()
            .email(uuid() + "@email.com")
            .build();

        SignUpRequestWrapper duplicated = SignUpRequestWrapper.builder()
            .member(dupSignedUp)
            .student(StudentInfoRequest.builder()
                .interestedField("백엔드")
                .status("휴학").build())
            .build();

        assertThatThrownBy(() -> memberService.signUp(duplicated))
            .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    @DisplayName("email 중복 예외")
    void duplicate_email_fail() {
        // given
        SignUpRequestWrapper signedUp = initStudent();
        memberService.signUp(signedUp);

        // when
        SignUpRequest dupSignedUp = signedUp.getMember()
            .toBuilder()
            .username(uuid())
            .build();

        SignUpRequestWrapper duplicated = SignUpRequestWrapper.builder()
            .member(dupSignedUp)
            .student(StudentInfoRequest.builder()
                .interestedField("백엔드")
                .status("휴학").build())
            .build();

        assertThatThrownBy(() -> memberService.signUp(duplicated))
            .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("username / email 조회 및 존재 여부 확인 성공")
    void find_and_exists_username_email_success() {
        // given
        SignUpRequestWrapper wrapper = initTutor();

        // when
        memberService.signUp(wrapper);
        String username = wrapper.getMember().getUsername();
        String email = wrapper.getMember().getEmail();

        // then
        assertThat(memberService.findByUsername(username).getEmail()).isEqualTo(email);
        assertThat(memberService.findByEmail(email).getUsername()).isEqualTo(username);
        assertThat(memberService.existsByUsername(username)).isTrue();
        assertThat(memberService.existsByEmail(email)).isTrue();

        assertThat(memberService.existsByUsername("nobody")).isFalse();
        assertThat(memberService.existsByEmail("nobody@email.com")).isFalse();
    }
}
