package com.example.techbridge.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreSignedUrlResponse {

    private String uploadUrl;
    private String key;
}
