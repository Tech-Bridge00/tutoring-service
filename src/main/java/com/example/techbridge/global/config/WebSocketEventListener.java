package com.example.techbridge.global.config;

import com.example.techbridge.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final NotificationService notificationService;

    // 세션별 사용자 정보 저장
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<Long, String> userSessionMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // 헤더에서 사용자 ID 추출 (클라이언트에서 연결 시 전송)
        String userIdStr = headerAccessor.getFirstNativeHeader("userId");
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                sessionUserMap.put(sessionId, userId);
                userSessionMap.put(userId, sessionId);

                // 사용자 온라인 상태 설정
                notificationService.setUserOnline(userId);

                log.info("사용자 연결: userId={}, sessionId={}", userId, sessionId);
            } catch (NumberFormatException e) {
                log.warn("잘못된 사용자 ID 형식: {}", userIdStr);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Long userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            userSessionMap.remove(userId);

            // 사용자 오프라인 상태 설정
            notificationService.setUserOffline(userId);

            log.info("사용자 연결 해제: userId={}, sessionId={}", userId, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();

        Long userId = sessionUserMap.get(sessionId);
        if (userId != null) {
            log.info("사용자 구독: userId={}, destination={}", userId, destination);

            // 특정 채팅방 구독 시 해당 방의 읽지 않은 알림들을 읽음 처리할 수 있음
            if (destination != null && destination.startsWith("/topic/room/")) {
                String roomIdStr = destination.substring("/topic/room/".length());
                try {
                    Long roomId = Long.parseLong(roomIdStr);
                    handleUserEnterRoom(userId, roomId);
                } catch (NumberFormatException e) {
                    log.warn("잘못된 채팅방 ID: {}", roomIdStr);
                }
            }
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Long userId = sessionUserMap.get(sessionId);
        if (userId != null) {
            log.info("사용자 구독 해제: userId={}, sessionId={}", userId, sessionId);
        }
    }

    // 사용자가 채팅방에 입장했을 때의 처리
    private void handleUserEnterRoom(Long userId, Long roomId) {
        // 해당 채팅방의 읽지 않은 메시지 알림들을 읽음 처리
        // 이는 선택적으로 구현할 수 있는 기능
        log.info("사용자가 채팅방에 입장: userId={}, roomId={}", userId, roomId);
    }

    // 특정 사용자의 온라인 상태 확인
    public boolean isUserOnline(Long userId) {
        return userSessionMap.containsKey(userId);
    }

    // 특정 사용자의 세션 ID 조회
    public String getUserSession(Long userId) {
        return userSessionMap.get(userId);
    }
}