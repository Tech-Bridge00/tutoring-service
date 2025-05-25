package com.example.techbridge.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.techbridge.domain.member.dto.MemberDetailResponse;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.example.techbridge.domain.member.support.AbstractMemberTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberQueryServiceTest extends AbstractMemberTestSupport {

    @Autowired
    private MemberQueryService memberQueryService;
    @Autowired
    private MemberCommandService memberCommandService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
    void findById_fail() {
        assertThatThrownBy(() -> memberQueryService.findById(999999L))
            .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("username / email 조회 및 존재 여부 확인 성공")
    void find_and_exists_username_email_success() {
        SignUpRequestWrapper wrapper = initTutor();
        memberCommandService.signUp(wrapper);

        String username = wrapper.getMember().getUsername();
        String email = wrapper.getMember().getEmail();

        assertThat(memberQueryService.findByUsername(username).getEmail()).isEqualTo(email);
        assertThat(memberQueryService.findByEmail(email).getUsername()).isEqualTo(username);
        assertThat(memberQueryService.existsByUsername(username)).isTrue();
        assertThat(memberQueryService.existsByEmail(email)).isTrue();
        assertThat(memberQueryService.existsByUsername("nobody")).isFalse();
        assertThat(memberQueryService.existsByEmail("nobody@email.com")).isFalse();
    }

    @Test
    @DisplayName("페이징 - 전체 회원 조회 기본 동작 검증")
    void findAll_paging_basic_success() {
        for (int i = 0; i < 15; i++) {
            memberCommandService.signUp(i % 2 == 0 ? initStudent() : initTutor());
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberDetailResponse> page = memberQueryService.findAll(null, pageable);

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(15);
        assertThat(page.getContent()).hasSize(10);
    }

    @Test
    @DisplayName("페이징 - role = STUDENT 조건 필터링 확인")
    void findAll_student_filter_success() {
        for (int i = 0; i < 5; i++) {
            memberCommandService.signUp(initStudent());
        }
        for (int i = 0; i < 3; i++) {
            memberCommandService.signUp(initTutor());
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberDetailResponse> page = memberQueryService.findAll(Role.STUDENT, pageable);

        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getContent()).allMatch(m -> m.getRole() == Role.STUDENT);
    }

    @Test
    @DisplayName("페이징 - 결과가 없는 조건에서 빈 페이지 반환")
    void findAll_no_result_empty_page() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberDetailResponse> page = memberQueryService.findAll(Role.STUDENT, pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
    }
}
