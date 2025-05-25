package com.example.techbridge.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long messageId;
    private Long senderId;
    private Long roomId;
    private String message;
    private LocalDateTime sentAt;
    private MessageType type;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}