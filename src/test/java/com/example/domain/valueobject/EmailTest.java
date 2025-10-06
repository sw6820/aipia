package com.example.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email Value Object 테스트")
class EmailTest {
    
    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {
        
        @Test
        @DisplayName("유효한 이메일로 생성 성공")
        void createWithValidEmail_Success() {
            // Given
            String validEmail = "test@example.com";
            
            // When
            Email email = Email.of(validEmail);
            
            // Then
            assertThat(email.toString()).isEqualTo("test@example.com");
        }
        
        @Test
        @DisplayName("대소문자 혼합 이메일은 소문자로 변환")
        void createWithMixedCaseEmail_ConvertsToLowerCase() {
            // Given
            String mixedCaseEmail = "Test@Example.COM";
            
            // When
            Email email = Email.of(mixedCaseEmail);
            
            // Then
            assertThat(email.toString()).isEqualTo("test@example.com");
        }
        
        @Test
        @DisplayName("공백이 있는 이메일은 트림됨")
        void createWithWhitespaceEmail_TrimsWhitespace() {
            // Given
            String emailWithWhitespace = "  test@example.com  ";
            
            // When
            Email email = Email.of(emailWithWhitespace);
            
            // Then
            assertThat(email.toString()).isEqualTo("test@example.com");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            "invalid-email",
            "@example.com",
            "test@",
            "test.example.com",
            "",
            "   ",
            "test@.com",
            "test@example.",
            "test@@example.com"
        })
        @DisplayName("유효하지 않은 이메일 형식으로 생성 실패")
        void createWithInvalidEmail_ThrowsException(String invalidEmail) {
            // When & Then
            assertThatThrownBy(() -> Email.of(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email format");
        }
        
        @Test
        @DisplayName("null 이메일로 생성 실패")
        void createWithNullEmail_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email cannot be null or empty");
        }
    }
    
    @Nested
    @DisplayName("도메인 관련 테스트")
    class DomainTest {
        
        @Test
        @DisplayName("특정 도메인 확인 성공")
        void hasDomain_WithMatchingDomain_ReturnsTrue() {
            // Given
            Email email = Email.of("test@example.com");
            
            // When & Then
            assertThat(email.hasDomain("example.com")).isTrue();
        }
        
        @Test
        @DisplayName("다른 도메인 확인 실패")
        void hasDomain_WithDifferentDomain_ReturnsFalse() {
            // Given
            Email email = Email.of("test@example.com");
            
            // When & Then
            assertThat(email.hasDomain("other.com")).isFalse();
        }
        
        @Test
        @DisplayName("로컬 파트 추출 성공")
        void getLocalPart_ReturnsCorrectLocalPart() {
            // Given
            Email email = Email.of("test.user@example.com");
            
            // When
            String localPart = email.getLocalPart();
            
            // Then
            assertThat(localPart).isEqualTo("test.user");
        }
        
        @Test
        @DisplayName("도메인 파트 추출 성공")
        void getDomainPart_ReturnsCorrectDomainPart() {
            // Given
            Email email = Email.of("test@example.com");
            
            // When
            String domainPart = email.getDomainPart();
            
            // Then
            assertThat(domainPart).isEqualTo("example.com");
        }
    }
    
    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {
        
        @Test
        @DisplayName("같은 이메일은 동등함")
        void equals_WithSameEmail_ReturnsTrue() {
            // Given
            Email email1 = Email.of("test@example.com");
            Email email2 = Email.of("test@example.com");
            
            // When & Then
            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }
        
        @Test
        @DisplayName("다른 이메일은 동등하지 않음")
        void equals_WithDifferentEmail_ReturnsFalse() {
            // Given
            Email email1 = Email.of("test1@example.com");
            Email email2 = Email.of("test2@example.com");
            
            // When & Then
            assertThat(email1).isNotEqualTo(email2);
        }
        
        @Test
        @DisplayName("대소문자 다른 이메일은 동등함")
        void equals_WithDifferentCase_ReturnsTrue() {
            // Given
            Email email1 = Email.of("Test@Example.COM");
            Email email2 = Email.of("test@example.com");
            
            // When & Then
            assertThat(email1).isEqualTo(email2);
        }
    }
}