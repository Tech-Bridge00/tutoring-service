package com.example.techbridge.domain.member.entity;

import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, length = 11)
    private String contact;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    private String profileImage;

    @Column(nullable = false, length = 50)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String location;

    private Long totalRating;
    private Long totalMatchCount;
    private Long totalClassCount;

    public enum Role {
        STUDENT, TUTOR
    }

    public enum Gender {
        M, F
    }

    public static Member of(SignUpRequest request, UnaryOperator<String> encoder) {
        return Member.builder()
            .username(request.getUsername())
            .password(encoder.apply(request.getPassword()))
            .name(request.getName())
            .nickname(request.getNickname())
            .age(request.getAge())
            .gender(request.getGender())
            .contact(request.getContact())
            .email(request.getEmail())
            .profileImage(request.getProfileImage())
            .status(request.getStatus())
            .role(request.getRole())
            .location(request.getLocation())
            .totalRating(0L)
            .totalMatchCount(0L)
            .totalClassCount(0L)
            .build();
    }

    public void encodePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}