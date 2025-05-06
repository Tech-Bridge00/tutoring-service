package com.example.techbridge.domain.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chatting_room")
public class ChattingRoom {
    @Id
    private String id;
    private Long roomId;
    private String roomName;
    private String roomState;
    private List<Long> participants;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
}