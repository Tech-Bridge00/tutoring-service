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
public class MemberUpdateWrapper {

    @Valid
    private MemberUpdateRequest member;

    @Valid
    private StudentUpdateRequest student;

    @Valid
    private TutorUpdateRequest tutor;
}
