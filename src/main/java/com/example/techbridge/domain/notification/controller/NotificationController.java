package com.example.techbridge.domain.notification.controller;

import com.example.techbridge.domain.notification.dto.NotificationDto;
import com.example.techbridge.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 사용자의 알림 목록 조회 (페이징)
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<NotificationDto>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<NotificationDto> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    @GetMapping("/users/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 특정 알림 읽음 처리
     */
    @PutMapping("/users/{userId}/read/{notificationId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long userId,
            @PathVariable String notificationId) {

        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PutMapping("/users/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/users/{userId}/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long userId,
            @PathVariable String notificationId) {

        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 온라인 상태 설정 (WebSocket 연결 시)
     */
    @MessageMapping("/notification.connect")
    public void userConnect(@Payload UserConnectionDto connectionDto) {
        notificationService.setUserOnline(connectionDto.getUserId());
    }

    /**
     * 사용자 오프라인 상태 설정 (WebSocket 연결 해제 시)
     */
    @MessageMapping("/notification.disconnect")
    public void userDisconnect(@Payload UserConnectionDto connectionDto) {
        notificationService.setUserOffline(connectionDto.getUserId());
    }

    /**
     * 수동 알림 테스트용 (개발/테스트 환경에서만 사용)
     */
    @PostMapping("/test")
    public ResponseEntity<Void> createTestNotification(@RequestBody NotificationDto notification) {
        notificationService.createAndSendNotification(notification);
        return ResponseEntity.ok().build();
    }

    // 사용자 연결 정보를 위한 내부 DTO
    public static class UserConnectionDto {
        private Long userId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
}