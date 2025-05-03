package com.example.techbridge.domain.member.support;

import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.dto.StudentInfoRequest;
import com.example.techbridge.domain.member.dto.TutorInfoRequest;
import com.example.techbridge.domain.member.entity.Member;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractMemberTestSupport {

    protected String uuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected String randomContact() {
        return "010" + ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);
    }

    protected SignUpRequestWrapper initStudent() {
        String uuid = uuid();

        SignUpRequest memberInfo = SignUpRequest.builder()
            .username("student" + uuid)
            .password("test1234")
            .name("홍길동")
            .nickname("학생_" + uuid)
            .age(21)
            .gender(Member.Gender.M)
            .contact(randomContact())
            .email("student_" + uuid + "@email.com")
            .status("대학생")
            .role(Member.Role.STUDENT)
            .location("서울 강남구")
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

    protected SignUpRequestWrapper initTutor() {
        String uuid = uuid();

        SignUpRequest memberInfo = SignUpRequest.builder()
            .username("tutor" + uuid)
            .password("test1234")
            .name("임꺽정")
            .nickname("튜터_" + uuid)
            .age(33)
            .gender(Member.Gender.F)
            .contact(randomContact())
            .email("tutor_" + uuid + "@email.com")
            .status("직장인")
            .role(Member.Role.TUTOR)
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
}
