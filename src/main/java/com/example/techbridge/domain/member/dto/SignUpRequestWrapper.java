package com.example.techbridge.domain.member.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpRequestWrapper {


    @Valid
    private SignUpRequest member;

    private StudentInfoRequest student;
    private TutorInfoRequest tutor;
}
