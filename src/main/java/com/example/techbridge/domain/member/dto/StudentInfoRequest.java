package com.example.techbridge.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentInfoRequest {

    @NotBlank(message = "관심 직무는 필수입니다.")
    private String interestedField;

    @NotBlank(message = "학적 상태는 필수입니다.")
    private String status;

}