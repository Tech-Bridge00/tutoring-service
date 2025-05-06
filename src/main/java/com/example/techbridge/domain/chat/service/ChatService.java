package com.example.techbridge.domain.chat.service;

import com.example.techbridge.domain.chat.dto.ChatMessageDto;
import com.example.techbridge.domain.chat.model.ChatParticipation;
import com.example.techbridge.domain.chat.model.ChattingRoom;
import com.example.techbridge.domain.chat.model.Message;
import com.example.techbridge.domain.chat.repository.ChatParticipationRepository;
import com.example.techbridge.domain.chat.repository.ChattingRoomRepository;
import com.example.techbridge.domain.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChattingRoomRepository chattingRoomRepository;
    private final MessageRepository messageRepository;
    private final ChatParticipationRepository chatParticipationRepository;
    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String CHAT_TOPIC = "chat-messages";

    // 1대1 채팅방 생성 또는 조회
    @Transactional
    public ChattingRoom getOrCreatePrivateRoom(Long user1Id, Long user2Id) {
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

        saveParticipant(user1Id, savedRoom.getRoomId());
        saveParticipant(user2Id, savedRoom.getRoomId());

        return savedRoom;
    }

    private void saveParticipant(Long userId, Long roomId) {
        ChatParticipation participation = ChatParticipation.builder()
                .userId(userId)
                .roomId(roomId)
                .joinedAt(LocalDateTime.now())
                .build();
        chatParticipationRepository.save(participation);
        System.out.println("저장된 참가자 정보: " + participation);
    }

    public void sendMessage(ChatMessageDto messageDto) {
        messageDto.setSentAt(LocalDateTime.now());
        kafkaTemplate.send(CHAT_TOPIC, messageDto);

        updateLastActivity(messageDto.getRoomId());
    }

    private void updateLastActivity(Long roomId) {
        ChattingRoom room = chattingRoomRepository.findByRoomId(roomId);
        if (room != null) {
            room.setLastActivityAt(LocalDateTime.now());
            chattingRoomRepository.save(room);
        }
    }


    @KafkaListener(topics = CHAT_TOPIC)
    public void receiveAndSendMessage(ChatMessageDto messageDto) {
        // MongoDB에 저장
        Message message = Message.builder()
                .messageId(messageDto.getMessageId())
                .senderId(messageDto.getSenderId())
                .roomId(messageDto.getRoomId())
                .content(messageDto.getMessage())
                .sentAt(messageDto.getSentAt())
                .type(messageDto.getType() == ChatMessageDto.MessageType.CHAT ?
                        Message.MessageType.CHAT :
                        messageDto.getType() == ChatMessageDto.MessageType.JOIN ?
                                Message.MessageType.JOIN : Message.MessageType.LEAVE)
                .build();
        messageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/room/" + messageDto.getRoomId(), messageDto);

        List<Long> participants = getParticipantIdsByRoomId(messageDto.getRoomId());
        for (Long participantId : participants) {
            if (!participantId.equals(messageDto.getSenderId())) {
                messagingTemplate.convertAndSendToUser(
                        participantId.toString(),
                        "/queue/messages",
                        messageDto
                );
            }
        }
    }

    public List<Message> getPreviousMessages(Long roomId) {
        return messageRepository.findByRoomIdOrderBySentAtDesc(roomId);
    }

    public List<ChattingRoom> getUserChatRooms(Long userId) {
        List<ChatParticipation> participations = chatParticipationRepository.findByUserId(userId);
        return participations.stream()
                .map(p -> {

                    ChattingRoom room = chattingRoomRepository.findByRoomId(p.getRoomId());
                    return room;
                })
                .filter(room -> room != null)
                .collect(Collectors.toList());
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
}