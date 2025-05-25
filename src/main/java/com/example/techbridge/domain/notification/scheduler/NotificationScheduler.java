package com.example.techbridge.domain.notification.scheduler;

import com.example.techbridge.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * 매일 새벽 2시에 오래된 알림 정리
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldNotifications() {
        try {
            log.info("오래된 알림 정리 작업 시작");
            notificationService.cleanupOldNotifications();
            log.info("오래된 알림 정리 작업 완료");
        } catch (Exception e) {
            log.error("오래된 알림 정리 작업 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 매 30분마다 오프라인 사용자 정리 (선택적)
     * Redis TTL로 자동 처리되지만 추가적인 정리를 위해
     */
    @Scheduled(fixedRate = 1800000) // 30분 = 30 * 60 * 1000ms
    public void cleanupOfflineUsers() {
        try {
            log.debug("오프라인 사용자 정리 작업 실행");
            // 필요한 경우 추가 정리 로직 구현
        } catch (Exception e) {
            log.error("오프라인 사용자 정리 작업 실패: {}", e.getMessage(), e);
        }
    }
}