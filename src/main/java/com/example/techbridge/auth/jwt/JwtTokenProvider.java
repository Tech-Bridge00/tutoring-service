package com.example.techbridge.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXP = Duration.ofMinutes(30).toMillis();
    private static final long REFRESH_TOKEN_EXP = Duration.ofDays(7).toMillis();

    @Value("${jwt.secret}")
    private String secret;
    private Key key;
    private JwtParser parser;

    @PostConstruct
    private void init() {
        byte[] bytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(bytes);
        this.parser = Jwts.parser()
            .setSigningKey(key)
            .build();
    }

    public String generateAccessToken(Long memberId, String role) {
        return generateToken(memberId, role, ACCESS_TOKEN_EXP);
    }

    public String generateRefreshToken(Long memberId, String role) {
        return generateToken(memberId, role, REFRESH_TOKEN_EXP);
    }

    private String generateToken(Long memberId, String role, long validity) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(memberId.toString())
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + validity))
            .signWith(key)
            .compact();
    }


    public Claims getClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    public Long extractMemberId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public boolean isValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("JWT validation failed: empty token");
            return false;
        }

        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }
}