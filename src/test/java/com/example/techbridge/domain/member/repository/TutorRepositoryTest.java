package com.example.techbridge.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.entity.Tutor;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TutorRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TutorRepository tutorRepository;

    private Member member;

    @BeforeEach
    void init() {
        member = Member.builder()
            .username("tutor1234")
            .password("password1234")
            .name("임꺽정")
            .nickname("백엔드짱")
            .age(25)
            .gender(Gender.F)
            .contact("01012345678")
            .email("tutor@example.com")
            .status("직장인")
            .role(Role.TUTOR)
            .location("대구")
            .build();

        member = memberRepository.save(member);
    }

    @Test
    @DisplayName("회원 ID로 Tutor 조회 성공")
    void findByMemberId_success() {
        // given
        Tutor tutor = Tutor.builder()
            .member(member)
            .introduction("안녕하세요. 반가워요.")
            .jobTitle("백엔드")
            .portfolioUrl("www.notion.so")
            .totalExperience(24)
            .currentlyEmployed(true)
            .build();

        tutorRepository.save(tutor);

        // when
        Optional<Tutor> foundMember = tutorRepository.findByMember_Id(member.getId());

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getMember().getUsername()).isEqualTo("tutor1234");
        assertThat(foundMember.get().getIntroduction()).isEqualTo("안녕하세요. 반가워요.");
        assertThat(foundMember.get().getTotalExperience()).isEqualTo(24);
        assertThat(foundMember.get().getCurrentlyEmployed()).isTrue();
    }
}