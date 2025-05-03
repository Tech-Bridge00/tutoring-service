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
import com.example.techbridge.domain.member.dto.TutorInfoRequest;
import com.example.techbridge.domain.member.dto.TutorUpdateRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.entity.Student;
import com.example.techbridge.domain.member.entity.Tutor;
import com.example.techbridge.domain.member.exception.EmailAlreadyExistsException;
import com.example.techbridge.domain.member.exception.SameAsOldPasswordException;
import com.example.techbridge.domain.member.exception.StudentInfoRequiredException;
import com.example.techbridge.domain.member.exception.TutorInfoRequiredException;
import com.example.techbridge.domain.member.exception.UnauthorizedException;
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
    private MemberCommandService memberCommandService;

    @Autowired
    private MemberQueryService memberQueryService;

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
        Member saved = memberCommandService.signUp(initStudent());

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
        Member saved = memberCommandService.signUp(initTutor());

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

        assertThatThrownBy(() -> memberCommandService.signUp(wrapper))
            .isInstanceOf(StudentInfoRequiredException.class)
            .hasMessageContaining("추가 정보");
    }

    @Test
    @DisplayName("TUTOR 추가정보 누락 예외 발생")
    void signUp_tutor_missingInfo_fail() {
        SignUpRequestWrapper wrapper = SignUpRequestWrapper.builder()
            .member(initTutor().getMember())
            .build();

        assertThatThrownBy(() -> memberCommandService.signUp(wrapper))
            .isInstanceOf(TutorInfoRequiredException.class)
            .hasMessageContaining("추가 정보");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        SignUpRequestWrapper wrapper = initStudent();
        Member savedMember = memberCommandService.signUp(wrapper);
        Long memberId = savedMember.getId();

        // when
        PasswordChangeRequest request = new PasswordChangeRequest("test1234", "newPwd5678");
        memberCommandService.changePassword(memberId, request);

        // then
        Member changed = memberRepository.findById(memberId).orElseThrow();
        assertThat(passwordEncoder.matches("newPwd5678", changed.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 실패")
    void changePassword_fail() {
        // given
        SignUpRequestWrapper wrapper = initStudent();
        Member savedMember = memberCommandService.signUp(wrapper);
        Long memberId = savedMember.getId();

        // when
        PasswordChangeRequest request = new PasswordChangeRequest("test1234", "test1234");

        // then
        assertThatThrownBy(() -> memberCommandService.changePassword(memberId, request))
            .isInstanceOf(SameAsOldPasswordException.class);
    }

    @Test
    @DisplayName("username 중복 예외")
    void duplicate_username_fail() {
        // given
        SignUpRequestWrapper signedUp = initStudent();
        memberCommandService.signUp(signedUp);

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

        assertThatThrownBy(() -> memberCommandService.signUp(duplicated))
            .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    @DisplayName("email 중복 예외")
    void duplicate_email_fail() {
        // given
        SignUpRequestWrapper signedUp = initStudent();
        memberCommandService.signUp(signedUp);

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

        assertThatThrownBy(() -> memberCommandService.signUp(duplicated))
            .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("username / email 조회 및 존재 여부 확인 성공")
    void find_and_exists_username_email_success() {
        // given
        SignUpRequestWrapper wrapper = initTutor();

        // when
        memberCommandService.signUp(wrapper);
        String username = wrapper.getMember().getUsername();
        String email = wrapper.getMember().getEmail();

        // then
        assertThat(memberQueryService.findByUsername(username).getEmail()).isEqualTo(email);
        assertThat(memberQueryService.findByEmail(email).getUsername()).isEqualTo(username);
        assertThat(memberQueryService.existsByUsername(username)).isTrue();
        assertThat(memberQueryService.existsByEmail(email)).isTrue();

        assertThat(memberQueryService.existsByUsername("nobody")).isFalse();
        assertThat(memberQueryService.existsByEmail("nobody@email.com")).isFalse();
    }

    @Test
    @DisplayName("STUDENT 정보 수정 성공")
    void update_student_success() {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        Long id = saved.getId();

        MemberUpdateWrapper updateRequest = MemberUpdateWrapper.builder()
            .member(MemberUpdateRequest.builder()
                .nickname("변경된닉네임")
                .contact("01099998888")
                .location("서울 중랑구")
                .build())
            .student(StudentUpdateRequest.builder()
                .interestedField("AI 백엔드")
                .status("졸업예정")
                .build())
            .build();

        // when
        Member updated = memberCommandService.updateMember(id, updateRequest, id);

        // then
        assertThat(updated.getNickname()).isEqualTo("변경된닉네임");
        assertThat(updated.getContact()).isEqualTo("01099998888");
        assertThat(updated.getLocation()).isEqualTo("서울 중랑구");
        assertThat(updated.getStudent().getInterestedField()).isEqualTo("AI 백엔드");
        assertThat(updated.getStudent().getStatus()).isEqualTo("졸업예정");
    }

    @Test
    @DisplayName("TUTOR 정보 수정 성공")
    void update_tutor_success() {
        // given
        Member saved = memberCommandService.signUp(initTutor());
        Long id = saved.getId();

        MemberUpdateWrapper updateRequest = MemberUpdateWrapper.builder()
            .member(MemberUpdateRequest.builder()
                .nickname("변경된튜터")
                .contact("01022223333")
                .location("서울 강서구")
                .build())
            .tutor(TutorUpdateRequest.builder()
                .introduction("10년차 백엔드 튜터입니다.")
                .jobTitle("시니어 개발자")
                .portfolioUrl("https://new-portfolio.com")
                .totalExperience(15)
                .currentlyEmployed(false)
                .build())
            .build();

        // when
        Member updated = memberCommandService.updateMember(id, updateRequest, id);

        // then
        assertThat(updated.getNickname()).isEqualTo("변경된튜터");
        assertThat(updated.getContact()).isEqualTo("01022223333");
        assertThat(updated.getLocation()).isEqualTo("서울 강서구");

        Tutor updatedTutor = updated.getTutor();
        assertThat(updatedTutor.getIntroduction()).isEqualTo("10년차 백엔드 튜터입니다.");
        assertThat(updatedTutor.getJobTitle()).isEqualTo("시니어 개발자");
        assertThat(updatedTutor.getPortfolioUrl()).isEqualTo("https://new-portfolio.com");
        assertThat(updatedTutor.getTotalExperience()).isEqualTo(15);
        assertThat(updatedTutor.getCurrentlyEmployed()).isFalse();
    }

    @Test
    @DisplayName("STUDENT 수정 시 student 정보 누락하면 예외 발생")
    void update_student_missingInfo_fail() {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        Long id = saved.getId();

        MemberUpdateWrapper updateRequest = MemberUpdateWrapper.builder()
            .member(MemberUpdateRequest.builder()
                .nickname("닉네임변경")
                .contact("01012341234")
                .location("서울 노원구")
                .build())
            .build();

        // when, then
        assertThatThrownBy(() -> memberCommandService.updateMember(id, updateRequest, id))
            .isInstanceOf(StudentInfoRequiredException.class);
    }

    @Test
    @DisplayName("TUTOR 수정 시 tutor 정보 누락하면 예외 발생")
    void update_tutor_missingInfo_fail() {
        // given
        Member saved = memberCommandService.signUp(initTutor());
        Long id = saved.getId();

        MemberUpdateWrapper updateRequest = MemberUpdateWrapper.builder()
            .member(MemberUpdateRequest.builder()
                .nickname("튜터 정보 변경")
                .contact("01077778888")
                .location("서울 마포구")
                .build())
            .build();

        // when, then
        assertThatThrownBy(() -> memberCommandService.updateMember(id, updateRequest, id))
            .isInstanceOf(TutorInfoRequiredException.class);
    }

    @Test
    @DisplayName("본인이 아닌 다른 사용자의 정보 수정 시도 → 예외 발생")
    void update_other_user_fail() {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        Long id = saved.getId();
        Long testAuthorId = id + 9999L;

        MemberUpdateWrapper updateRequest = MemberUpdateWrapper.builder()
            .member(MemberUpdateRequest.builder()
                .nickname("변경시도")
                .contact("01012345678")
                .location("서울 서초구")
                .build())
            .student(StudentUpdateRequest.builder()
                .interestedField("게임 개발")
                .status("복학예정")
                .build())
            .build();

        // when, then
        assertThatThrownBy(() -> memberCommandService.updateMember(id, updateRequest, testAuthorId))
            .isInstanceOf(UnauthorizedException.class);
    }

}
