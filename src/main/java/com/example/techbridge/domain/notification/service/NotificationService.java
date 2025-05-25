package com.example.techbridge.domain.notification.service;

import com.example.techbridge.domain.notification.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Redis Key 상수들
    private static final String USER_NOTIFICATIONS_KEY = "notifications:user:";
    private static final String UNREAD_COUNT_KEY = "notifications:unread:";
    private static final String USER_ONLINE_KEY = "users:online:";
    private static final String NOTIFICATION_SETTINGS_KEY = "notifications:settings:";

    // 알림 TTL (7일)
    private static final long NOTIFICATION_TTL_DAYS = 7;

    /**
     * 새 알림 생성 및 전송
     */
    public void createAndSendNotification(NotificationDto notification) {
        try {
            // 알림 ID 생성
            String notificationId = UUID.randomUUID().toString();
            notification.setNotificationId(notificationId);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);

            // Redis에 알림 저장 (Sorted Set 사용 - 시간순 정렬)
            String userNotificationsKey = USER_NOTIFICATIONS_KEY + notification.getUserId();
            double score = notification.getCreatedAt().toEpochSecond(ZoneOffset.UTC);

            redisTemplate.opsForZSet().add(userNotificationsKey, notification, score);

            // TTL 설정
            redisTemplate.expire(userNotificationsKey, NOTIFICATION_TTL_DAYS, TimeUnit.DAYS);

            // 읽지 않은 알림 수 증가
            incrementUnreadCount(notification.getUserId());

            // 사용자가 온라인이면 실시간 알림 전송
            if (isUserOnline(notification.getUserId())) {
                sendRealTimeNotification(notification);
            }

            log.info("알림 생성 완료: userId={}, type={}, message={}",
                    notification.getUserId(), notification.getType(), notification.getMessage());

        } catch (Exception e) {
            log.error("알림 생성 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 메시지 기반 알림 생성
     */
    public void createMessageNotification(Long receiverId, Long senderId, Long roomId,
                                          String message, String senderName) {
        NotificationDto notification = NotificationDto.builder()
                .userId(receiverId)
                .senderId(senderId)
                .roomId(roomId)
                .title("새 메시지")
                .message(senderName + ": " + (message.length() > 50 ?
                        message.substring(0, 50) + "..." : message))
                .type(NotificationDto.NotificationType.NEW_MESSAGE)
                .senderName(senderName)
                .build();

        createAndSendNotification(notification);
    }

    /**
     * 사용자 입장/퇴장 알림
     */
    public void createUserActivityNotification(Long userId, Long roomId, String userName,
                                               NotificationDto.NotificationType type) {
        String message = type == NotificationDto.NotificationType.USER_JOIN ?
                userName + "님이 채팅방에 입장했습니다." :
                userName + "님이 채팅방을 나갔습니다.";

        NotificationDto notification = NotificationDto.builder()
                .userId(userId)
                .roomId(roomId)
                .title("채팅방 활동")
                .message(message)
                .type(type)
                .build();

        createAndSendNotification(notification);
    }

    /**
     * 사용자의 모든 알림 조회 (페이징)
     */
    public List<NotificationDto> getUserNotifications(Long userId, int page, int size) {
        String key = USER_NOTIFICATIONS_KEY + userId;
        long start = (long) page * size;
        long end = start + size - 1;

        Set<Object> notifications = redisTemplate.opsForZSet()
                .reverseRange(key, start, end);

        return notifications.stream()
                .map(obj -> {
                    try {
                        return objectMapper.convertValue(obj, NotificationDto.class);
                    } catch (Exception e) {
                        log.error("알림 변환 실패: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(notification -> notification != null)
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 알림 수 조회
     */
    public long getUnreadCount(Long userId) {
        String key = UNREAD_COUNT_KEY + userId;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }

    /**
     * 알림 읽음 처리
     */
    public void markAsRead(Long userId, String notificationId) {
        String key = USER_NOTIFICATIONS_KEY + userId;

        // 알림 조회
        Set<ZSetOperations.TypedTuple<Object>> notifications =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        for (ZSetOperations.TypedTuple<Object> tuple : notifications) {
            try {
                NotificationDto notification = objectMapper.convertValue(
                        tuple.getValue(), NotificationDto.class);

                if (notification.getNotificationId().equals(notificationId) && !notification.isRead()) {
                    notification.setRead(true);

                    // 업데이트된 알림으로 교체
                    redisTemplate.opsForZSet().remove(key, tuple.getValue());
                    redisTemplate.opsForZSet().add(key, notification, tuple.getScore());

                    // 읽지 않은 수 감소
                    decrementUnreadCount(userId);
                    break;
                }
            } catch (Exception e) {
                log.error("알림 읽음 처리 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    public void markAllAsRead(Long userId) {
        String key = USER_NOTIFICATIONS_KEY + userId;
        Set<ZSetOperations.TypedTuple<Object>> notifications =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

        for (ZSetOperations.TypedTuple<Object> tuple : notifications) {
            try {
                NotificationDto notification = objectMapper.convertValue(
                        tuple.getValue(), NotificationDto.class);

                if (!notification.isRead()) {
                    notification.setRead(true);
                    redisTemplate.opsForZSet().remove(key, tuple.getValue());
                    redisTemplate.opsForZSet().add(key, notification, tuple.getScore());
                }
            } catch (Exception e) {
                log.error("알림 일괄 읽음 처리 실패: {}", e.getMessage());
            }
        }

        // 읽지 않은 수 초기화
        redisTemplate.opsForValue().set(UNREAD_COUNT_KEY + userId, 0);
    }

    /**
     * 사용자 온라인 상태 설정
     */
    public void setUserOnline(Long userId) {
        String key = USER_ONLINE_KEY + userId;
        redisTemplate.opsForValue().set(key, "online", 30, TimeUnit.MINUTES);
    }

    /**
     * 사용자 오프라인 상태 설정
     */
    public void setUserOffline(Long userId) {
        String key = USER_ONLINE_KEY + userId;
        redisTemplate.delete(key);
    }

    /**
     * 사용자 온라인 상태 확인
     */
    public boolean isUserOnline(Long userId) {
        String key = USER_ONLINE_KEY + userId;
        return redisTemplate.hasKey(key);
    }

    /**
     * 실시간 알림 전송
     */
    private void sendRealTimeNotification(NotificationDto notification) {
        try {
            // 사용자별 개인 큐로 전송
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId().toString(),
                    "/queue/notifications",
                    notification
            );

            log.info("실시간 알림 전송 완료: userId={}", notification.getUserId());
        } catch (Exception e) {
            log.error("실시간 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 읽지 않은 알림 수 증가
     */
    private void incrementUnreadCount(Long userId) {
        String key = UNREAD_COUNT_KEY + userId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, NOTIFICATION_TTL_DAYS, TimeUnit.DAYS);
    }

    /**
     * 읽지 않은 알림 수 감소
     */
    private void decrementUnreadCount(Long userId) {
        String key = UNREAD_COUNT_KEY + userId;
        Long count = redisTemplate.opsForValue().decrement(key);
        if (count != null && count < 0) {
            redisTemplate.opsForValue().set(key, 0);
        }
    }

    /**
     * 알림 삭제
     */
    public void deleteNotification(Long userId, String notificationId) {
        String key = USER_NOTIFICATIONS_KEY + userId;
        Set<Object> notifications = redisTemplate.opsForZSet().range(key, 0, -1);

        for (Object obj : notifications) {
            try {
                NotificationDto notification = objectMapper.convertValue(obj, NotificationDto.class);
                if (notification.getNotificationId().equals(notificationId)) {
                    redisTemplate.opsForZSet().remove(key, obj);
                    if (!notification.isRead()) {
                        decrementUnreadCount(userId);
                    }
                    break;
                }
            } catch (Exception e) {
                log.error("알림 삭제 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * 오래된 알림 정리 (스케줄링으로 사용)
     */
    public void cleanupOldNotifications() {
        // 7일 이전 알림들을 정리하는 로직
        // 실제 운영에서는 스케줄러로 주기적 실행
        long cutoffTime = LocalDateTime.now().minusDays(NOTIFICATION_TTL_DAYS)
                .toEpochSecond(ZoneOffset.UTC);

        Set<String> userKeys = redisTemplate.keys(USER_NOTIFICATIONS_KEY + "*");
        for (String key : userKeys) {
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, cutoffTime);
        }

        log.info("오래된 알림 정리 완료");
    }
}