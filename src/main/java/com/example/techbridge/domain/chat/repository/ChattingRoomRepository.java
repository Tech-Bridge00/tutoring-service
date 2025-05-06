package com.example.techbridge.domain.chat.repository;

import com.example.techbridge.domain.chat.model.ChattingRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChattingRoomRepository extends MongoRepository<ChattingRoom, String> {
    ChattingRoom findByRoomId(String roomId);
}