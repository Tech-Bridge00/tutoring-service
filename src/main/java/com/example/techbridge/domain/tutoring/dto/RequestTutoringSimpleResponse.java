package com.example.techbridge.domain.tutoring.dto;

import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestTutoringSimpleResponse implements TutoringSimpleResponse{

    private Long tutoringId;

    private String receiverName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String location;

    private RequestStatus status;

    public static RequestTutoringSimpleResponse from(Tutoring tutoring) {
        return RequestTutoringSimpleResponse.builder()
            .tutoringId(tutoring.getId())
            .receiverName(tutoring.getReceiver().getName())
            .startTime(tutoring.getStartTime())
            .endTime(tutoring.getEndTime())
            .location(tutoring.getLocation())
            .status(tutoring.getRequestStatus())
            .build();
    }
}
