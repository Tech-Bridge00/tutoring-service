package com.example.techbridge.domain.member.dto;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Student;
import com.example.techbridge.domain.member.entity.Tutor;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequestWrapper {

    @Valid
    private SignUpRequest member;

    private StudentInfoRequest student;
    private TutorInfoRequest tutor;

    public Member toMemberEntity() {
        return Member.builder()
            .username(member.getUsername())
            .password(member.getPassword()) // 테스트용
            .name(member.getName())
            .nickname(member.getNickname())
            .age(member.getAge())
            .gender(member.getGender())
            .contact(member.getContact())
            .email(member.getEmail())
            .status(member.getStatus())
            .role(member.getRole())
            .location(member.getLocation())
            .build();
    }

    public Student toStudentEntity(Member member) {
        if (student == null) {
            return null;
        }

        return Student.builder()
            .member(member)
            .interestedField(student.getInterestedField())
            .status(student.getStatus())
            .build();
    }

    public Tutor toTutorEntity(Member member) {
        if (tutor == null) {
            return null;
        }

        return Tutor.builder()
            .member(member)
            .introduction(tutor.getIntroduction())
            .jobTitle(tutor.getJobTitle())
            .portfolioUrl(tutor.getPortfolioUrl())
            .totalExperience(tutor.getTotalExperience())
            .currentlyEmployed(tutor.getCurrentlyEmployed())
            .build();
    }
}
