package com.example.techbridge.auth.repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {

    private static final String PREFIX = "refresh:";
    private static final long REFRESH_TOKEN_EXP = Duration.ofDays(7).toMillis();

    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set(PREFIX + memberId, refreshToken, REFRESH_TOKEN_EXP, TimeUnit.MILLISECONDS);
    }

    public Optional<String> findByMemberId(Long memberId) {
        String val = redisTemplate.opsForValue().get(PREFIX + memberId);
        return Optional.ofNullable(val);
    }

    public void delete(Long memberId) {
        redisTemplate.delete(PREFIX + memberId);
    }
}
