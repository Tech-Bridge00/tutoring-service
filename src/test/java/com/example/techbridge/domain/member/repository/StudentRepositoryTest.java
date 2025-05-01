package com.example.techbridge.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.entity.Student;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class StudentRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StudentRepository studentRepository;

    private Member member;

    @BeforeEach
    void init() {
        member = Member.builder()
            .username("student1234")
            .password("password1234")
            .name("임꺽정")
            .nickname("백엔드짱")
            .age(25)
            .gender(Gender.F)
            .contact("01012345678")
            .email("student@example.com")
            .status("대학생")
            .role(Role.STUDENT)
            .location("대구")
            .build();

        member = memberRepository.save(member);
    }

    @Test
    @DisplayName("회원 ID로 Student 조회 성공")
    void findByMemberId_success() {
        // given
        Student student = Student.builder()
            .member(member)
            .interestedField("백엔드")
            .status("대학생")
            .build();

        studentRepository.save(student);

        // when
        Optional<Student> foundMember = studentRepository.findByMember_Id(member.getId());

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getMember().getUsername()).isEqualTo("student1234");
        assertThat(foundMember.get().getInterestedField()).isEqualTo("백엔드");
    }
}