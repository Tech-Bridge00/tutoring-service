package com.example.techbridge.domain.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "message")
public class Message {
    @Id
    private String id;
    private Long messageId;
    private Long senderId;
    private Long roomId;
    private String content;
    private LocalDateTime sentAt;
    private MessageType type;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}