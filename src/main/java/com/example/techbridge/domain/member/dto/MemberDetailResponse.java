package com.example.techbridge.domain.member.dto;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import java.util.Optional;
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
    private String profileImage;
    private Role role;
    private String status;
    private String location;
    private Long totalRating;
    private Long totalMatchCount;
    private Long totalClassCount;

    private StudentPart student;
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
        return MemberDetailResponse.builder()
            .id(m.getId())
            .username(m.getUsername())
            .name(m.getName())
            .nickname(m.getNickname())
            .age(m.getAge())
            .gender(m.getGender())
            .contact(m.getContact())
            .email(m.getEmail())
            .profileImage(m.getProfileImage())
            .role(m.getRole())
            .status(m.getStatus())
            .location(m.getLocation())
            .totalRating(m.getTotalRating())
            .totalMatchCount(m.getTotalMatchCount())
            .totalClassCount(m.getTotalClassCount())
            .student(Optional.ofNullable(m.getStudent())
                .map(s -> StudentPart.builder()
                    .interestedField(s.getInterestedField())
                    .status(s.getStatus())
                    .build())
                .orElse(null))
            .tutor(Optional.ofNullable(m.getTutor())
                .map(t -> TutorPart.builder()
                    .introduction(t.getIntroduction())
                    .jobTitle(t.getJobTitle())
                    .portfolioUrl(t.getPortfolioUrl())
                    .totalExperience(t.getTotalExperience())
                    .currentlyEmployed(t.getCurrentlyEmployed())
                    .build())
                .orElse(null))
            .build();
    }
}