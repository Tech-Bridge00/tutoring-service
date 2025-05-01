package com.example.techbridge.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
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
    private PasswordEncoder passwordEncoder;

    private SignUpRequest request;

    @BeforeEach
    void init() {
        request = new SignUpRequest();
        ReflectionTestUtils.setField(request, "username", "userfortest");
        ReflectionTestUtils.setField(request, "password", "test1234");
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "nickname", "아이티보이");
        ReflectionTestUtils.setField(request, "age", 29);
        ReflectionTestUtils.setField(request, "gender", Gender.M);
        ReflectionTestUtils.setField(request, "contact", "01012345678");
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "status", "대학생");
        ReflectionTestUtils.setField(request, "role", Role.STUDENT);
        ReflectionTestUtils.setField(request, "location", "서울");
    }

    @Test
    @DisplayName("회원 가입 성공")
    void signUp_success() {
        // given
        SignUpRequest signUpRequest = this.request;

        // when
        Member member = memberService.signUp(signUpRequest);

        // then
        assertThat(memberRepository.findByUsername("userfortest")).isPresent();
        assertThat(passwordEncoder.matches("test1234", member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        // given
        memberService.signUp(request);

        // when
        memberService.changePassword("userfortest", "test1234", "newpassword1234");

        // then
        Member changed = memberRepository.findByUsername("userfortest").orElseThrow();
        assertThat(passwordEncoder.matches("newpassword1234", changed.getPassword())).isTrue();
    }

    @Test
    @DisplayName("아이디로 회원 조회 성공")
    void findByUsername_success() {
        // given
        memberService.signUp(request);

        // when
        Member found = memberService.findByUsername("userfortest");

        // then
        assertThat(found.getNickname()).isEqualTo("아이티보이");
    }

    @Test
    @DisplayName("이메일로 회원 조회 성공")
    void findByEmail_success() {
        // given
        memberService.signUp(request);

        // when
        Member found = memberService.findByEmail("test@example.com");

        // then
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("아이디 중복 확인")
    void existsByUsername() {
        // given
        memberService.signUp(request);

        // when
        boolean exists = memberService.existsByUsername("userfortest");
        boolean notExists = memberService.existsByUsername("nobody");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("이메일 중복 확인")
    void existsByEmail() {
        // given
        memberService.signUp(request);

        // when
        boolean exists = memberService.existsByEmail("test@example.com");
        boolean notExists = memberService.existsByEmail("nobody@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
