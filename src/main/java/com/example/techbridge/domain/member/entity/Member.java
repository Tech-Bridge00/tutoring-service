package com.example.techbridge.domain.member.entity;

import com.example.techbridge.domain.member.dto.MemberUpdateRequest;
import com.example.techbridge.domain.member.dto.SignUpRequest;
import com.example.techbridge.global.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
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

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    private String profileImageKey;

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

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Student student;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Tutor tutor;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    public enum Role {
        STUDENT, TUTOR,
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
            .profileImageKey(request.getProfileImageKey())
            .status(request.getStatus())
            .role(request.getRole())
            .location(request.getLocation())
            .totalRating(0L)
            .totalMatchCount(0L)
            .totalClassCount(0L)
            .build();
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
        if (tutor.getMember() != this) {
            tutor.setMember(this);
        }
    }

    public void setStudent(Student student) {
        this.student = student;
        if (student.getMember() != this) {
            student.setMember(this);
        }
    }

    public void updateProfile(MemberUpdateRequest request) {
        if (request.getNickname() != null) {
            this.nickname = request.getNickname();
        }

        if (request.getEmail() != null) {
            this.email = request.getEmail();
        }

        if (request.getAge() != null) {
            this.age = request.getAge();
        }

        if (request.getContact() != null) {
            this.contact = request.getContact();
        }

        if (request.getLocation() != null) {
            this.location = request.getLocation();
        }

        if (request.getProfileImageKey() != null) {
            this.profileImageKey = request.getProfileImageKey();
        }
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateDeleted() {
        this.deleted = true;
        updateDeletedAt(LocalDateTime.now());
    }

    public void encodePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}