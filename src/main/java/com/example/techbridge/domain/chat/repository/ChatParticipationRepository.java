package com.example.techbridge.domain.chat.repository;

import com.example.techbridge.domain.chat.model.ChatParticipation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatParticipationRepository extends MongoRepository<ChatParticipation, String> {
    List<ChatParticipation> findByRoomId(Long roomId);
    List<ChatParticipation> findByUserId(Long userId);
}