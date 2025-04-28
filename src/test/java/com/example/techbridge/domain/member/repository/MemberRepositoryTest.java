package com.example.techbridge.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Role;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member member() {
        return Member.builder()
            .username("test")
            .password("test1234")
            .name("홍길동")
            .nickname("아이티보이")
            .age(24)
            .gender("M")
            .contact("01012345678")
            .email("test@example.com")
            .status("학생")
            .role(Role.STUDENT)
            .location("서울")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("회원 가입 및 조회")
    void saveAndFindMember_success() {
        // given
        Member given = memberRepository.save(member());

        // when
        Member found = memberRepository.findById(given.getId()).orElseThrow();

        // then
        assertThat(found)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt")
            .isEqualTo(given);
    }

    @Test
    @DisplayName("아이디로 회원 조회")
    void findByUsername_whenExists_returnsMember() {
        // given
        memberRepository.save(member());

        // when
        Member found = memberRepository.findByUsername("test").orElseThrow();

        // then
        assertThat(found.getNickname()).isEqualTo("아이티보이");
    }

    @Test
    @DisplayName("아이디로 회원 조회")
    void findByEmail_whenExists_returnsMember() {
        // given
        memberRepository.save(member());

        // when
        Member found = memberRepository.findByEmail("test@example.com").orElseThrow();

        // then
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("아이디 중복 확인")
    void existsByUsername() {
        // given
        memberRepository.save(member());

        // expect
        assertThat(memberRepository.existsByUsername("test")).isTrue();
        assertThat(memberRepository.existsByUsername("test1234")).isFalse();
    }

    @Test
    @DisplayName("이메일 중복 확인")
    void existsByEmail() {
        // given
        memberRepository.save(member());

        // expect
        assertThat(memberRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(memberRepository.existsByEmail("test1234@example.com")).isFalse();
    }
}