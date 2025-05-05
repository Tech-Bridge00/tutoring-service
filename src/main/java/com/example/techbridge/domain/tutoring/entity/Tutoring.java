package com.example.techbridge.domain.tutoring.entity;

import com.example.techbridge.domain.tutoring.dto.TutoringRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.List;
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
public class Tutoring extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String location;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    public void updateStatus(RequestStatus status) {
        this.requestStatus = status;
    }

    public enum RequestStatus {
        CREATED, ACCEPTED, REJECTED, IN_PROGRESS, COMPLETED, CANCELED;

        public boolean canAcceptOrReject() {
            return this == CREATED;
        }

        public boolean canBeCanceled() {
            return this == CREATED || this == ACCEPTED;
        }

        public static List<RequestStatus> activeStatues() {
            return List.of(ACCEPTED, IN_PROGRESS);
        }
    }

    public static Tutoring of(TutoringRequest request, Member requester, Member receiver, RequestStatus requestStatus) {
        return Tutoring.builder()
            .requester(requester)
            .receiver(receiver)
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .location(request.getLocation())
            .requestStatus(requestStatus)
            .build();
    }
}
