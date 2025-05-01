package com.example.techbridge.domain.member.dto;

import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SignUpRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotNull(message = "나이는 필수입니다.")
    private Integer age;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @NotBlank(message = "연락처는 필수입니다.")
    private String contact;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    private String profileImage;

    @NotBlank(message = "현재 상태 정보는 필수입니다.")
    private String status;

    @NotNull(message = "역할 정보는 필수입니다.")
    private Role role;

    @NotBlank(message = "위치 정보는 필수입니다.")
    private String location;

}
