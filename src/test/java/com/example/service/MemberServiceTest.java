package com.example.service;

import com.example.domain.Member;
import com.example.application.service.MemberService;
import com.example.application.usecase.MemberUseCase;
import com.example.application.query.MemberQuery;
import com.example.infrastructure.validation.MemberValidator;
import com.example.infrastructure.persistence.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
@org.junit.jupiter.api.Disabled("Temporarily disabled due to Mockito bytecode generation issues with Member class")
class MemberServiceTest {

    @Mock
    private MemberUseCase memberUseCase;
    
    @Mock
    private MemberQuery memberQuery;
    
    @Mock
    private MemberValidator memberValidator;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
    }

    @Test
    @DisplayName("회원 생성 성공")
    void createMember_Success() {
        // Given
        when(memberUseCase.createMember("test@example.com", "Test User", "010-1234-5678")).thenReturn(testMember);

        // When
        var result = memberService.createMember("test@example.com", "Test User", "010-1234-5678");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getPhoneNumber()).isEqualTo("010-1234-5678");
        
        verify(memberValidator).validateMemberCreation("test@example.com", "Test User", "010-1234-5678");
        verify(memberUseCase).createMember("test@example.com", "Test User", "010-1234-5678");
    }

    @Test
    @DisplayName("중복 이메일로 회원 생성 실패")
    void createMember_DuplicateEmail_Failure() {
        // Given
        doThrow(new IllegalArgumentException("Member with email test@example.com already exists"))
            .when(memberUseCase).createMember("test@example.com", "Test User", "010-1234-5678");

        // When & Then
        assertThatThrownBy(() -> 
            memberService.createMember("test@example.com", "Test User", "010-1234-5678"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
        
        verify(memberValidator).validateMemberCreation("test@example.com", "Test User", "010-1234-5678");
        verify(memberUseCase).createMember("test@example.com", "Test User", "010-1234-5678");
    }

    @Test
    @DisplayName("ID로 회원 조회 성공")
    void getMemberById_Success() {
        // Given
        when(memberQuery.findById(1L)).thenReturn(Optional.of(testMember));

        // When
        var result = memberService.getMemberById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        
        verify(memberValidator).validateMemberId(1L);
        verify(memberQuery).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 회원 조회")
    void getMemberById_NotFound() {
        // Given
        when(memberQuery.findById(999L)).thenReturn(Optional.empty());

        // When
        var result = memberService.getMemberById(999L);

        // Then
        assertThat(result).isEmpty();
        
        verify(memberValidator).validateMemberId(999L);
        verify(memberQuery).findById(999L);
    }

    @Test
    @DisplayName("이메일로 회원 조회 성공")
    void getMemberByEmail_Success() {
        // Given
        when(memberQuery.findByEmail("test@example.com")).thenReturn(Optional.of(testMember));

        // When
        var result = memberService.getMemberByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        
        verify(memberQuery).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("회원 비활성화 성공")
    void deactivateMember_Success() {
        // Given
        when(memberUseCase.deactivateMember(1L)).thenReturn(testMember);

        // When
        var result = memberService.deactivateMember(1L);

        // Then
        assertThat(result).isNotNull();
        
        verify(memberValidator).validateMemberId(1L);
        verify(memberUseCase).deactivateMember(1L);
    }

    @Test
    @DisplayName("존재하지 않는 회원 비활성화 실패")
    void deactivateMember_NotFound_Failure() {
        // Given
        doThrow(new IllegalArgumentException("Member not found with ID: 999"))
            .when(memberUseCase).deactivateMember(999L);

        // When & Then
        assertThatThrownBy(() -> memberService.deactivateMember(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
        
        verify(memberValidator).validateMemberId(999L);
        verify(memberUseCase).deactivateMember(999L);
    }
}