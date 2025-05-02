package com.example.techbridge.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MemberUpdateRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 4, max = 64)
    private String nickname;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    @Size(max = 64)
    private String email;

    @NotNull(message = "나이는 필수입니다.")
    @Min(1)
    @Max(100)
    private Integer age;

    @NotBlank(message = "연락처는 필수입니다.")
    @Pattern(regexp = "\\d{11}", message = "연락처는 숫자 11자리만 입력해주세요.")
    private String contact;

    @NotBlank(message = "위치 정보는 필수입니다.")
    private String location;
}
