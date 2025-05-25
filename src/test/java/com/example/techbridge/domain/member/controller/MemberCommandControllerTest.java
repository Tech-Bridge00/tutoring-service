package com.example.techbridge.domain.member.controller;

import static com.example.techbridge.domain.member.support.LoginRequestPostProcessor.loginMember;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.techbridge.domain.member.dto.MemberUpdateWrapper;
import com.example.techbridge.domain.member.dto.PasswordChangeRequest;
import com.example.techbridge.domain.member.dto.SignUpRequestWrapper;
import com.example.techbridge.domain.member.dto.StudentUpdateRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.service.MemberCommandService;
import com.example.techbridge.domain.member.support.AbstractMemberTestSupport;
import com.example.techbridge.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberCommandControllerTest extends AbstractMemberTestSupport {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberCommandService memberCommandService;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() throws Exception {
        // given
        SignUpRequestWrapper request = initStudent();

        // when
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // then
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.username").value(request.getMember().getUsername()));
    }

    @Test
    @DisplayName("회원가입 실패 - username 중복")
    void signUp_usernameDuplicate_fail() throws Exception {
        // given
        SignUpRequestWrapper signed = initStudent();
        memberCommandService.signUp(signed);

        // when
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signed)))
            // then
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.USERNAME_ALREADY_EXISTS.getCode()));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        PasswordChangeRequest request = new PasswordChangeRequest("test1234", "newPassword123");

        // when
        mockMvc.perform(patch("/api/members/" + saved.getId() + "/password")
                .with(loginMember(saved))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // then
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 잘못된 ID")
    void changePassword_notFound() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        long invalidId = saved.getId() + 123;
        PasswordChangeRequest request = new PasswordChangeRequest("wrongPassword", "newPassword");

        // when
        mockMvc.perform(patch("/api/members/" + invalidId + "/password")
                .with(loginMember(saved))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // then
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.MEMBER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void updateMemberInfo_success() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        MemberUpdateWrapper request = MemberUpdateWrapper.builder()
            .member(defaultMemberUpdate(saved))
            .student(defaultStudentUpdate(saved.getStudent()))
            .build();

        // when
        mockMvc.perform(patch("/api/members/" + saved.getId())
                .with(loginMember(saved))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nickname").value(request.getMember().getNickname()))
            .andExpect(jsonPath("$.data.contact").value(request.getMember().getContact()));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 본인이 아닌 경우")
    void updateMemberInfo_forbidden() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        Member other = memberCommandService.signUp(initStudent());
        MemberUpdateWrapper request = MemberUpdateWrapper.builder()
            .member(defaultMemberUpdate(saved))
            .student(StudentUpdateRequest.builder()
                .interestedField("AI 테스트").status("재학중").build())
            .build();

        // when
        mockMvc.perform(patch("/api/members/" + saved.getId())
                .with(loginMember(other))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // then
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - studentInfo 누락")
    void updateMemberInfo_missingStudentInfo() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        MemberUpdateWrapper request = MemberUpdateWrapper.builder()
            .member(defaultMemberUpdate(saved))
            .build();

        // when
        mockMvc.perform(patch("/api/members/" + saved.getId())
                .with(loginMember(saved))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // then
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.STUDENT_INFO_REQUIRED.getCode()));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 존재하지 않는 ID")
    void updateMemberInfo_notFound() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        long invalidId = saved.getId() + 987;
        MemberUpdateWrapper request = MemberUpdateWrapper.builder()
            .student(StudentUpdateRequest.builder()
                .interestedField("실패용 관심 직무").status("실패용 상태").build())
            .build();

        // when
        mockMvc.perform(patch("/api/members/" + invalidId)
                .with(loginMember(saved))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            // then
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.MEMBER_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 본인")
    void deleteMember_success() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());

        // when, then
        mockMvc.perform(delete("/api/members/" + saved.getId())
                .with(loginMember(saved)))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 본인이 아닌 경우")
    void deleteMember_forbidden() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        Member other = memberCommandService.signUp(initStudent());

        // when, then - other 로 로그인해서 saved 탈퇴하면 403
        mockMvc.perform(delete("/api/members/" + saved.getId())
                .with(loginMember(other)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 ID")
    void deleteMember_notFound() throws Exception {
        // given
        Member saved = memberCommandService.signUp(initStudent());
        long invalidId = saved.getId() + 123;

        // when, then - 없는 ID 탈퇴시 404 + MEMBER_NOT_FOUND
        mockMvc.perform(delete("/api/members/" + invalidId)
                .with(loginMember(saved)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.MEMBER_NOT_FOUND.getCode()));
    }
}