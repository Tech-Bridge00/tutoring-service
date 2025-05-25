package com.example.techbridge.domain.chat.service;

import com.example.techbridge.domain.chat.dto.ChatMessageDto;
import com.example.techbridge.domain.chat.model.ChatParticipation;
import com.example.techbridge.domain.chat.model.ChattingRoom;
import com.example.techbridge.domain.chat.model.Message;
import com.example.techbridge.domain.chat.repository.ChatParticipationRepository;
import com.example.techbridge.domain.chat.repository.ChattingRoomRepository;
import com.example.techbridge.domain.chat.repository.MessageRepository;
import com.example.techbridge.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChattingRoomRepository chattingRoomRepository;
    private final MessageRepository messageRepository;
    private final ChatParticipationRepository chatParticipationRepository;
    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    private static final String CHAT_TOPIC = "chat-messages";

    @Transactional
    public ChattingRoom getOrCreatePrivateRoom(Long user1Id, Long user2Id) {
        // 기존 채팅방 검색
        List<ChatParticipation> user1Rooms = chatParticipationRepository.findByUserId(user1Id);
        List<ChatParticipation> user2Rooms = chatParticipationRepository.findByUserId(user2Id);

        List<Long> user1RoomIds = user1Rooms.stream()
                .map(ChatParticipation::getRoomId)
                .collect(Collectors.toList());

        Optional<Long> sharedRoomId = user2Rooms.stream()
                .map(ChatParticipation::getRoomId)
                .filter(user1RoomIds::contains)
                .findFirst();

        if (sharedRoomId.isPresent()) {
            ChattingRoom room = chattingRoomRepository.findByRoomId(sharedRoomId.get());
            if (room != null) {
                return room;
            }
        }

        // 새 채팅방 생성
        Long newRoomId = generateRoomId();
        String roomName = "Private: " + user1Id + " & " + user2Id;
        ChattingRoom room = ChattingRoom.builder()
                .roomId(newRoomId)
                .roomName(roomName)
                .roomState("ACTIVE")
                .participants(Arrays.asList(user1Id, user2Id))
                .createdAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();

        ChattingRoom savedRoom = chattingRoomRepository.save(room);

        // 참가자 정보 저장
        saveParticipant(user1Id, savedRoom.getRoomId());
        saveParticipant(user2Id, savedRoom.getRoomId());

        // 채팅방 생성 알림
        notificationService.createUserActivityNotification(
                user2Id, savedRoom.getRoomId(), getUserName(user1Id),
                com.example.techbridge.domain.notification.dto.NotificationDto.NotificationType.ROOM_CREATED);

        notificationService.createUserActivityNotification(
                user1Id, savedRoom.getRoomId(), getUserName(user2Id),
                com.example.techbridge.domain.notification.dto.NotificationDto.NotificationType.ROOM_CREATED);

        return savedRoom;
    }

    private void saveParticipant(Long userId, Long roomId) {
        ChatParticipation participation = ChatParticipation.builder()
                .userId(userId)
                .roomId(roomId)
                .joinedAt(LocalDateTime.now())
                .build();
        chatParticipationRepository.save(participation);
        log.info("참가자 정보 저장 완료: userId={}, roomId={}", userId, roomId);
    }

    public void sendMessage(ChatMessageDto messageDto) {
        try {
            messageDto.setSentAt(LocalDateTime.now());
            log.info("Kafka로 메시지 전송: {}", messageDto);
            kafkaTemplate.send(CHAT_TOPIC, messageDto);
            updateLastActivity(messageDto.getRoomId());
        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메시지 전송에 실패했습니다.", e);
        }
    }

    private void updateLastActivity(Long roomId) {
        try {
            ChattingRoom room = chattingRoomRepository.findByRoomId(roomId);
            if (room != null) {
                room.setLastActivityAt(LocalDateTime.now());
                chattingRoomRepository.save(room);
            }
        } catch (Exception e) {
            log.error("채팅방 활동 시간 업데이트 실패: roomId={}, error={}", roomId, e.getMessage());
        }
    }

    @KafkaListener(topics = CHAT_TOPIC)
    public void receiveAndSendMessage(ChatMessageDto messageDto) {
        try {
            log.info("Kafka에서 메시지 수신: {}", messageDto);

            // MongoDB에 메시지 저장
            Message message = Message.builder()
                    .messageId(messageDto.getMessageId())
                    .senderId(messageDto.getSenderId())
                    .roomId(messageDto.getRoomId())
                    .content(messageDto.getMessage())
                    .sentAt(messageDto.getSentAt())
                    .type(convertMessageType(messageDto.getType()))
                    .build();
            messageRepository.save(message);

            log.info("메시지 저장 완료: {}", message);

            // 채팅방 전체에 메시지 전송
            messagingTemplate.convertAndSend("/topic/room/" + messageDto.getRoomId(), messageDto);

            // 참가자들에게 개별 처리
            List<Long> participants = getParticipantIdsByRoomId(messageDto.getRoomId());
            processParticipantNotifications(messageDto, participants);

        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", e.getMessage(), e);
        }
    }

    private void processParticipantNotifications(ChatMessageDto messageDto, List<Long> participants) {
        for (Long participantId : participants) {
            try {
                log.info("사용자에게 메시지 전송: {}", participantId);

                // 개별 사용자에게 메시지 전송
                messagingTemplate.convertAndSendToUser(
                        participantId.toString(),
                        "/queue/messages",
                        messageDto
                );

                // 알림 처리
                processNotificationForParticipant(messageDto, participantId);

            } catch (Exception e) {
                log.error("참가자 알림 처리 실패: participantId={}, error={}", participantId, e.getMessage());
            }
        }
    }

    private void processNotificationForParticipant(ChatMessageDto messageDto, Long participantId) {
        // 발신자가 아닌 참가자들에게만 알림 처리
        if (participantId.equals(messageDto.getSenderId())) {
            return;
        }

        String senderName = getUserName(messageDto.getSenderId());

        switch (messageDto.getType()) {
            case CHAT:
                // 사용자가 오프라인이거나 다른 채팅방에 있을 때만 알림 생성
                if (!isUserActiveInCurrentRoom(participantId, messageDto.getRoomId())) {
                    notificationService.createMessageNotification(
                            participantId,
                            messageDto.getSenderId(),
                            messageDto.getRoomId(),
                            messageDto.getMessage(),
                            senderName
                    );
                }
                break;

            case JOIN:
                notificationService.createUserActivityNotification(
                        participantId,
                        messageDto.getRoomId(),
                        senderName,
                        com.example.techbridge.domain.notification.dto.NotificationDto.NotificationType.USER_JOIN
                );
                break;

            case LEAVE:
                notificationService.createUserActivityNotification(
                        participantId,
                        messageDto.getRoomId(),
                        senderName,
                        com.example.techbridge.domain.notification.dto.NotificationDto.NotificationType.USER_LEAVE
                );
                break;
        }
    }

    private boolean isUserActiveInCurrentRoom(Long userId, Long roomId) {
        // 현재는 단순히 온라인 여부만 체크
        // 실제 구현에서는 사용자가 현재 보고 있는 채팅방 정보를 확인해야 함
        return notificationService.isUserOnline(userId);
    }

    private String getUserName(Long userId) {
        // TODO: 실제 사용자 서비스에서 이름 조회
        // 현재는 임시로 "User + ID" 형태로 반환
        return "User" + userId;
    }

    private Message.MessageType convertMessageType(ChatMessageDto.MessageType type) {
        switch (type) {
            case CHAT: return Message.MessageType.CHAT;
            case JOIN: return Message.MessageType.JOIN;
            case LEAVE: return Message.MessageType.LEAVE;
            default: return Message.MessageType.CHAT;
        }
    }

    public List<Message> getPreviousMessages(Long roomId) {
        try {
            return messageRepository.findByRoomIdOrderBySentAtDesc(roomId);
        } catch (Exception e) {
            log.error("이전 메시지 조회 실패: roomId={}, error={}", roomId, e.getMessage());
            return List.of();
        }
    }

    public List<ChattingRoom> getUserChatRooms(Long userId) {
        try {
            List<ChatParticipation> participations = chatParticipationRepository.findByUserId(userId);
            return participations.stream()
                    .map(p -> chattingRoomRepository.findByRoomId(p.getRoomId()))
                    .filter(room -> room != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("사용자 채팅방 목록 조회 실패: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }

    private List<Long> getParticipantIdsByRoomId(Long roomId) {
        return chatParticipationRepository.findByRoomId(roomId)
                .stream()
                .map(ChatParticipation::getUserId)
                .collect(Collectors.toList());
    }

    private Long generateRoomId() {
        return System.currentTimeMillis();
    }

    public void userJoinRoom(Long userId, Long roomId) {
        notificationService.setUserOnline(userId);
        log.info("사용자 채팅방 입장: userId={}, roomId={}", userId, roomId);
    }

    public void userLeaveRoom(Long userId, Long roomId) {
        log.info("사용자 채팅방 퇴장: userId={}, roomId={}", userId, roomId);
    }
}