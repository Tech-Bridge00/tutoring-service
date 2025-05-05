package com.example.techbridge.auth.jwt;

import com.example.techbridge.domain.member.entity.Member.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
            .setId(jti)
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

    public Role extractRole(String token) {
        Claims claims = getClaims(token);
        String role = claims.get("role", String.class);
        return Role.valueOf(role);
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

    public String resolveBearer(HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }

        return null;
    }

    public String getJti(String token) {
        return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getId();
    }

    public Duration getRemainingTTL(String token) {
        Date exp = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
        return Duration.between(Instant.now(), exp.toInstant());
    }
}