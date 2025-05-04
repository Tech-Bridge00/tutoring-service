package com.example.techbridge.domain.member.entity;

import com.example.techbridge.domain.member.dto.TutorUpdateRequest;
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
public class Tutor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String introduction;
    private String jobTitle;
    private String portfolioUrl;
    private Integer totalExperience;
    private Boolean currentlyEmployed;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    public void updateProfile(TutorUpdateRequest request) {
        if (request.getIntroduction() != null) {
            this.introduction = request.getIntroduction();
        }

        if (request.getJobTitle() != null) {
            this.jobTitle = request.getJobTitle();
        }

        if (request.getPortfolioUrl() != null) {
            this.portfolioUrl = request.getPortfolioUrl();
        }

        if (request.getTotalExperience() != null) {
            this.totalExperience = request.getTotalExperience();
        }

        if (request.getCurrentlyEmployed() != null) {
            this.currentlyEmployed = request.getCurrentlyEmployed();
        }
    }

    public void updateDeleted() {
        this.deleted = true;
        this.updateDeletedAt(LocalDateTime.now());
    }
}
