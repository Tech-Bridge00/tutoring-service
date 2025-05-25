package com.example.techbridge.domain.member.dto;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Gender;
import com.example.techbridge.domain.member.entity.Member.Role;
import lombok.Getter;

@Getter
public class MemberResponse {

    private final Long id;
    private final String username;
    private final String name;
    private final String nickname;
    private final Integer age;
    private final Gender gender;
    private final String contact;
    private final String email;
    private final String profileImageKey;
    private final Role role;
    private final String status;
    private final String location;
    private final Long totalRating;
    private final Long totalMatchCount;
    private final Long totalClassCount;

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.age = member.getAge();
        this.gender = member.getGender();
        this.contact = member.getContact();
        this.email = member.getEmail();
        this.profileImageKey = member.getProfileImageKey();
        this.role = member.getRole();
        this.status = member.getStatus();
        this.location = member.getLocation();
        this.totalRating = member.getTotalRating();
        this.totalMatchCount = member.getTotalMatchCount();
        this.totalClassCount = member.getTotalClassCount();
    }
}