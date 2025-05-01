package com.example.techbridge.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

}
