package com.example.controller;

import com.example.domain.Member;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.web.MemberController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("MemberController 통합 테스트")
class MemberControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("회원 생성 API 테스트")
    void createMember_Success() throws Exception {
        // Given
        var request = new MemberController.CreateMemberRequest();
        request.setEmail("test@example.com");
        request.setName("Test User");
        request.setPhoneNumber("010-1234-5678");

        // When & Then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 회원 생성 실패")
    void createMember_InvalidEmail_Failure() throws Exception {
        // Given
        var request = new MemberController.CreateMemberRequest();
        request.setEmail("invalid-email");
        request.setName("Test User");
        request.setPhoneNumber("010-1234-5678");

        // When & Then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 전화번호 형식으로 회원 생성 실패")
    void createMember_InvalidPhoneNumber_Failure() throws Exception {
        // Given
        var request = new MemberController.CreateMemberRequest();
        request.setEmail("test@example.com");
        request.setName("Test User");
        request.setPhoneNumber("01012345678"); // 하이픈 없음

        // When & Then
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원 조회 API 테스트")
    void getMemberById_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        member = memberRepository.save(member);

        // When & Then
        mockMvc.perform(get("/api/members/{id}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회")
    void getMemberById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/members/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이메일로 회원 조회 API 테스트")
    void getMemberByEmail_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        memberRepository.save(member);

        // When & Then
        mockMvc.perform(get("/api/members/email/{email}", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("모든 회원 조회 API 테스트")
    void getAllMembers_Success() throws Exception {
        // Given
        Member member1 = Member.builder()
                .email("test1@example.com")
                .name("Test User 1")
                .phoneNumber("010-1111-1111")
                .build();
        memberRepository.save(member1);

        Member member2 = Member.builder()
                .email("test2@example.com")
                .name("Test User 2")
                .phoneNumber("010-2222-2222")
                .build();
        memberRepository.save(member2);

        // When & Then
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("회원 비활성화 API 테스트")
    void deactivateMember_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        member = memberRepository.save(member);

        // When & Then
        mockMvc.perform(put("/api/members/{id}/deactivate", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("회원 활성화 API 테스트")
    void activateMember_Success() throws Exception {
        // Given
        Member member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        member.deactivate();
        member = memberRepository.save(member);

        // When & Then
        mockMvc.perform(put("/api/members/{id}/activate", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}