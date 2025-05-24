package com.example.techbridge.domain.chat.controller;

import com.example.techbridge.domain.chat.dto.ChatMessageDto;
import com.example.techbridge.domain.chat.model.ChattingRoom;
import com.example.techbridge.domain.chat.model.Message;
import com.example.techbridge.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    // STOMP를 통한 메시지 처리
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessage) {
        chatService.sendMessage(chatMessage);
    }

    // 채팅방 입장
    @MessageMapping("/chat.join")
    public void joinChat(@Payload ChatMessageDto chatMessage) {
        chatMessage.setType(ChatMessageDto.MessageType.JOIN);
        chatService.sendMessage(chatMessage);
    }

    // 채팅방 퇴장
    @MessageMapping("/chat.leave")
    public void leaveChat(@Payload ChatMessageDto chatMessage) {
        chatMessage.setType(ChatMessageDto.MessageType.LEAVE);
        chatService.sendMessage(chatMessage);
    }

    // 1대1 채팅방 생성 또는 조회
    @PostMapping("/rooms/private")
    public ResponseEntity<ChattingRoom> getOrCreatePrivateRoom(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        return ResponseEntity.ok(chatService.getOrCreatePrivateRoom(user1Id, user2Id));
    }

    // 사용자의 모든 채팅방 목록 조회
    @GetMapping("/users/{userId}/rooms")
    public ResponseEntity<List<ChattingRoom>> getUserChatRooms(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getUserChatRooms(userId));
    }

    // 채팅방 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<Message>> getRoomMessages(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getPreviousMessages(roomId));
    }
}