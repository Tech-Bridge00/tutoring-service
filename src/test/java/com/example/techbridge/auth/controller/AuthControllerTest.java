package com.example.techbridge.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.techbridge.auth.dto.LoginRequest;
import com.example.techbridge.auth.dto.RefreshRequest;
import com.example.techbridge.auth.jwt.JwtTokenProvider;
import com.example.techbridge.auth.repository.RefreshTokenRepository;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        Member member = Member.builder()
            .username("tester")
            .password(passwordEncoder.encode("1234"))
            .name("홍길동")
            .nickname("테스터입니다")
            .age(29)
            .gender(Member.Gender.M)
            .contact("01012345678")
            .email("test@example.com")
            .profileImage(null)
            .status("대학생")
            .role(Member.Role.STUDENT)
            .location("서울시 강남구")
            .totalRating(0L)
            .totalMatchCount(0L)
            .totalClassCount(0L)
            .build();

        memberRepository.save(member);
    }

    @Test
    void login_success_accessToken_refreshToken_response() throws Exception {
        LoginRequest request = new LoginRequest("tester", "1234");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void login_fail_throw_exception() throws Exception {
        LoginRequest request = new LoginRequest("nobody", "wrong");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void using_refreshToken_get_new_accessToken() throws Exception {
        LoginRequest request = new LoginRequest("tester", "1234");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse)
            .path("data")
            .path("refreshToken")
            .asText();

        RefreshRequest refreshRequest = new RefreshRequest(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));
    }

    @Test
    void when_logout_delete_refreshToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest("tester", "1234");

        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse)
            .path("data")
            .path("accessToken")
            .asText();

        String refreshToken = objectMapper.readTree(loginResponse)
            .path("data")
            .path("refreshToken")
            .asText();

        RefreshRequest logoutRequest = new RefreshRequest(refreshToken);

        mockMvc.perform(post("/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"));

        Long memberId = jwtTokenProvider.extractMemberId(refreshToken);
        Optional<String> deletedToken = refreshTokenRepository.findByMemberId(memberId);

        assertThat(deletedToken).isEmpty();
    }
}