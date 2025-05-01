package com.example.techbridge.domain.member.dto;

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
}
