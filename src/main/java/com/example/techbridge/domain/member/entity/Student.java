package com.example.techbridge.domain.member.entity;

import com.example.techbridge.domain.member.dto.StudentUpdateRequest;
import com.example.techbridge.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Student extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String interestedField;
    private String status;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    public void updateProfile(StudentUpdateRequest request) {
        if (request.getInterestedField() != null) {
            this.interestedField = request.getInterestedField();
        }

        if (request.getStatus() != null) {
            this.status = request.getStatus();
        }
    }

    public void updateDeleted() {
        this.deleted = true;
        this.updateDeletedAt(LocalDateTime.now());
    }
}
