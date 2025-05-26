package com.example.techbridge.domain.tutoring.dto;

import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReceiveTutoringSimpleResponse {

    private Long tutoringId;

    private String requesterName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String location;

    private RequestStatus status;

    public static ReceiveTutoringSimpleResponse from(Tutoring tutoring) {
        return ReceiveTutoringSimpleResponse.builder()
            .tutoringId(tutoring.getId())
            .requesterName(tutoring.getRequester().getName())
            .startTime(tutoring.getStartTime())
            .endTime(tutoring.getEndTime())
            .location(tutoring.getLocation())
            .status(tutoring.getRequestStatus())
            .build();
    }
}
