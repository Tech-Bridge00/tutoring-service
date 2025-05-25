package com.example.techbridge.domain.member.controller;

import static com.example.techbridge.domain.member.support.LoginRequestPostProcessor.loginMember;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.service.MemberCommandService;
import com.example.techbridge.domain.member.support.AbstractMemberTestSupport;
import com.example.techbridge.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberQueryControllerTest extends AbstractMemberTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberCommandService memberCommandService;

    @Test
    @DisplayName("조건 없는 회원 목록 조회 성공")
    void findAllMembers_success() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());

        // when, then
        mockMvc.perform(get("/api/members")
                .with(loginMember(saved)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content[0].id").value(saved.getId()));
    }

    @Test
    @DisplayName("username/email 체크 시 쿼리 파라미터 누락 → 400")
    void checkUsernameEmail_missingParams() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());

        // when, then
        mockMvc.perform(get("/api/members/check")
                .with(loginMember(saved)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 404 반환")
    void findById_notFound() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        long invalidId = saved.getId() + 999;

        // when, then
        mockMvc.perform(get("/api/members/" + invalidId)
                .with(loginMember(saved)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.MEMBER_NOT_FOUND.getCode()));
    }
}
