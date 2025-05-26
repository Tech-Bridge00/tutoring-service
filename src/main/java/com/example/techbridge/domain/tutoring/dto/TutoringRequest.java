package com.example.techbridge.domain.tutoring.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutoringRequest {

    private Long requesterId;

    private Long receiverId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String location;
}
