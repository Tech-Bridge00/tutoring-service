package com.example.techbridge.domain.member.dto;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Tutor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TutorInfoRequest {

    @NotBlank(message = "소개글은 필수입니다.")
    private String introduction;

    @NotBlank(message = "직무는 필수입니다.")
    private String jobTitle;

    private String portfolioUrl;

    @NotNull(message = "총 경력은 필수입니다.")
    private Integer totalExperience;

    @NotNull(message = "현직 유무는 필수입니다.")
    private Boolean currentlyEmployed;

    public Tutor toEntity(Member member) {
        return Tutor.builder()
            .member(member)
            .introduction(introduction)
            .jobTitle(jobTitle)
            .portfolioUrl(portfolioUrl)
            .totalExperience(totalExperience)
            .currentlyEmployed(currentlyEmployed)
            .build();
    }
}
