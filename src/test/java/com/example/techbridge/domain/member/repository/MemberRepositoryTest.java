package com.example.techbridge.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.entity.Student;
import com.example.techbridge.domain.member.entity.Tutor;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private EntityManager em;

    private Member member() {
        return Member.builder()
            .username("test")
            .password("test1234")
            .name("홍길동")
            .nickname("아이티보이")
            .age(24)
            .gender(Gender.M)
            .contact("01012345678")
            .email("test@example.com")
            .status("학생")
            .role(Role.STUDENT)
            .location("서울")
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
    @DisplayName("이메일로 회원 조회")
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

    @Test
    @DisplayName("회원 논리적 탈퇴 - delete 호출 후 findById 결과 미조회")
    void softDelete_findById() {
        Member member = memberRepository.save(member());
        memberRepository.delete(member);
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }

    @Test
    @DisplayName("회원 논리적 탈퇴 - delete 호출 후 findAll 결과 미조회")
    void softDelete_findAll() {
        Member member = memberRepository.save(member());
        memberRepository.delete(member);
        assertThat(memberRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("회원 논리적 탈퇴 - delete 호출 후 findByUsername 결과 미조회")
    void softDelete_findByUsername() {
        Member member = memberRepository.save(member());
        memberRepository.delete(member);
        assertThat(memberRepository.findByUsername("testUser")).isEmpty();
    }

    @Test
    @DisplayName("회원 논리적 탈퇴 - delete 호출 후 findByEmail 결과 미조회")
    void softDelete_findByEmail() {
        Member member = memberRepository.save(member());
        memberRepository.delete(member);
        assertThat(memberRepository.findByEmail("test@example.com")).isEmpty();
    }

    @Test
    @DisplayName("전체 조회 페이징 기능 검증")
    void findAll_pagination_returnsPage() {
        Member member = memberRepository.save(member());
        Pageable pageable = PageRequest.of(0, 5);
        Page<Member> page = memberRepository.findAll(pageable);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).contains(member);
    }

    @Test
    @DisplayName("역할별 조회 기능 검증 - STUDENT 필터링")
    void findByRole_filtering_returnsOnlyMatchingRole() {
        Member student = memberRepository.save(member());
        Member tutor = Member.builder()
            .username("tutorUser")
            .password("test1234")
            .name("홍길동")
            .nickname("튜터봇")
            .age(30)
            .gender(Gender.F)
            .contact("01087654321")
            .email("tutor@example.com")
            .status("직장인")
            .role(Role.TUTOR)
            .location("부산")
            .build();

        memberRepository.save(tutor);

        Pageable pageable = PageRequest.of(0, 5);
        Page<Member> studentPage = memberRepository.findByRole(Role.STUDENT, pageable);
        Page<Member> tutorPage = memberRepository.findByRole(Role.TUTOR, pageable);

        assertThat(studentPage.getTotalElements()).isEqualTo(1);
        assertThat(studentPage.getContent()).contains(student);
        assertThat(tutorPage.getTotalElements()).isEqualTo(1);
        assertThat(tutorPage.getContent()).contains(tutor);
    }

    @Test
    @DisplayName("상세 조회 검증 - EntityGraph 로딩")
    void findWithDetailsById_loadsOnlyStudent() {
        Member member = memberRepository.save(member());
        Student student = Student.builder()
            .member(member)
            .interestedField("백엔드")
            .status("재학중")
            .build();

        studentRepository.save(student);

        em.flush();
        em.clear();

        Optional<Member> foundMember = memberRepository.findWithDetailsById(member.getId());

        assertThat(foundMember).isPresent();
        Member found = foundMember.get();
        assertThat(found.getStudent()).isNotNull();
        assertThat(found.getStudent().getInterestedField()).isEqualTo("백엔드");
        assertThat(found.getTutor()).isNull();
    }

    @Test
    @DisplayName("상세 조회 검증 - tutor 정보")
    void findWithDetailsById_tutor() {
        Member member = Member.builder()
            .username("tutorUser2")
            .password("test1234")
            .name("임꺽정")
            .nickname("튜터킹")
            .age(32)
            .gender(Gender.F)
            .contact("01011223344")
            .email("tutor2@example.com")
            .status("직장인")
            .role(Role.TUTOR)
            .location("대구")
            .build();

        member = memberRepository.save(member);

        Tutor tutor = Tutor.builder()
            .member(member)
            .introduction("튜터소개")
            .jobTitle("백엔드")
            .portfolioUrl("url")
            .totalExperience(5)
            .currentlyEmployed(true)
            .build();

        tutorRepository.save(tutor);

        em.flush();
        em.clear();

        Optional<Member> foundMember = memberRepository.findWithDetailsById(member.getId());
        assertThat(foundMember).isPresent();
        Member found = foundMember.get();
        assertThat(found.getTutor()).isNotNull();
        assertThat(found.getTutor().getIntroduction()).isEqualTo("튜터소개");
        assertThat(found.getStudent()).isNull();
    }
}