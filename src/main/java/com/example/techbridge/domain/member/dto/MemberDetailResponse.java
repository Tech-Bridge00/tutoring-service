package com.example.techbridge.domain.member.dto;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.entity.Student;
import com.example.techbridge.domain.member.entity.Tutor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberDetailResponse {

    private Long id;
    private String username;
    private String name;
    private String nickname;
    private Integer age;
    private Gender gender;
    private String contact;
    private String email;
    private String profileImageKey;
    private Role role;
    private String status;
    private String location;
    private Long totalRating;
    private Long totalMatchCount;
    private Long totalClassCount;

    @JsonInclude(Include.NON_NULL)
    private StudentPart student;
    @JsonInclude(Include.NON_NULL)
    private TutorPart tutor;

    @Getter
    @Builder
    public static class StudentPart {

        private String interestedField;
        private String status;
    }

    @Getter
    @Builder
    public static class TutorPart {

        private String introduction;
        private String jobTitle;
        private String portfolioUrl;
        private Integer totalExperience;
        private Boolean currentlyEmployed;
    }

    public static MemberDetailResponse of(Member m) {
        Student student = m.getStudent();
        Tutor tutor = m.getTutor();

        return MemberDetailResponse.builder()
            .id(m.getId())
            .username(m.getUsername())
            .name(m.getName())
            .nickname(m.getNickname())
            .age(m.getAge())
            .gender(m.getGender())
            .contact(m.getContact())
            .email(m.getEmail())
            .profileImageKey(m.getProfileImageKey())
            .role(m.getRole())
            .status(m.getStatus())
            .location(m.getLocation())
            .totalRating(m.getTotalRating())
            .totalMatchCount(m.getTotalMatchCount())
            .totalClassCount(m.getTotalClassCount())
            .student(student != null ? StudentPart.builder()
                .interestedField(student.getInterestedField())
                .status(student.getStatus())
                .build() : null)
            .tutor(tutor != null ? TutorPart.builder()
                .introduction(tutor.getIntroduction())
                .jobTitle(tutor.getJobTitle())
                .portfolioUrl(tutor.getPortfolioUrl())
                .totalExperience(tutor.getTotalExperience())
                .currentlyEmployed(tutor.getCurrentlyEmployed())
                .build() : null)
            .build();
    }
}