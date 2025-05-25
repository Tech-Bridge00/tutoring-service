package com.example.techbridge.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String notificationId;
    private Long userId;
    private Long senderId;
    private Long roomId;
    private String title;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String senderName; // 발신자 이름 (선택적)

    public enum NotificationType {
        NEW_MESSAGE,    // 새 메시지
        USER_JOIN,      // 사용자 입장
        USER_LEAVE,     // 사용자 퇴장
        ROOM_CREATED    // 채팅방 생성
    }
}

// 읽음 상태 업데이트용 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class NotificationReadDto {
    private String notificationId;
    private Long userId;
    private boolean isRead;
    private LocalDateTime readAt;
}

// 알림 설정 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class NotificationSettingsDto {
    private Long userId;
    private boolean enablePushNotification;
    private boolean enableEmailNotification;
    private boolean enableChatNotification;
    private boolean enableSoundNotification;
}