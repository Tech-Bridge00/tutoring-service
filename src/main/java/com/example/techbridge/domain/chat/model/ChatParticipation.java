package com.example.techbridge.domain.chat.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_participation")
public class ChatParticipation {
    @Id
    private String id;
    private Long userId;
    private Long roomId;
    private LocalDateTime joinedAt;
}