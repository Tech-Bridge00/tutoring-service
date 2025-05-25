package com.example.techbridge.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_KEY_PREFIX = "refresh:";

    public void deleteByMemberId(Long memberId) {
        String key = REFRESH_KEY_PREFIX + memberId;
        redisTemplate.delete(key);
    }
}
