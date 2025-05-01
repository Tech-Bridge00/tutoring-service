package com.example.techbridge.domain.member.dto;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Student;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StudentInfoRequest {

    @NotBlank(message = "관심 직무는 필수입니다.")
    private String interestedField;

    @NotBlank(message = "학적 상태는 필수입니다.")
    private String status;

    public Student toEntity(Member member) {
        return Student.builder()
            .member(member)
            .interestedField(interestedField)
            .status(status)
            .build();
    }
}