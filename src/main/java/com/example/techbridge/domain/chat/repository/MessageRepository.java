package com.example.techbridge.domain.chat.repository;

import com.example.techbridge.domain.chat.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByRoomIdOrderBySentAtDesc(Long roomId);
}