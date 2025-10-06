package com.example.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PhoneNumber Value Object 테스트")
class PhoneNumberTest {
    
    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {
        
        @Test
        @DisplayName("유효한 한국 전화번호로 생성 성공")
        void createWithValidKoreanPhoneNumber_Success() {
            // Given
            String koreanPhone = "010-1234-5678";
            
            // When
            PhoneNumber phoneNumber = PhoneNumber.korean(koreanPhone);
            
            // Then
            assertThat(phoneNumber.toString()).isEqualTo("010-1234-5678");
            assertThat(phoneNumber.isKorean()).isTrue();
            assertThat(phoneNumber.isInternational()).isFalse();
        }
        
        @Test
        @DisplayName("유효한 국제 전화번호로 생성 성공")
        void createWithValidInternationalPhoneNumber_Success() {
            // Given
            String internationalPhone = "+1234567890";
            
            // When
            PhoneNumber phoneNumber = PhoneNumber.international(internationalPhone);
            
            // Then
            assertThat(phoneNumber.toString()).isEqualTo("+1234567890");
            assertThat(phoneNumber.isKorean()).isFalse();
            assertThat(phoneNumber.isInternational()).isTrue();
        }
        
        @Test
        @DisplayName("공백이 있는 전화번호는 트림됨")
        void createWithWhitespacePhoneNumber_TrimsWhitespace() {
            // Given
            String phoneWithWhitespace = "  010-1234-5678  ";
            
            // When
            PhoneNumber phoneNumber = PhoneNumber.korean(phoneWithWhitespace);
            
            // Then
            assertThat(phoneNumber.toString()).isEqualTo("010-1234-5678");
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            "010-123-456",      // 잘못된 형식
            "010-12345-6789",  // 잘못된 형식
            "01012345678",     // 하이픈 없음
            "010-1234-567",    // 마지막 자리 부족
            "010-1234-56789",  // 마지막 자리 초과
            "",
            "   ",
            "invalid-phone"
        })
        @DisplayName("유효하지 않은 한국 전화번호 형식으로 생성 실패")
        void createWithInvalidKoreanPhoneNumber_ThrowsException(String invalidPhone) {
            // When & Then
            assertThatThrownBy(() -> PhoneNumber.korean(invalidPhone))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid phone number format");
        }
        
        @Test
        @DisplayName("null 전화번호로 생성 실패")
        void createWithNullPhoneNumber_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> PhoneNumber.korean(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Phone number cannot be null or empty");
        }
    }
    
    @Nested
    @DisplayName("형식화 테스트")
    class FormattingTest {
        
        @Test
        @DisplayName("한국 전화번호 형식화 성공")
        void getFormatted_WithKoreanPhone_ReturnsFormattedString() {
            // Given
            PhoneNumber koreanPhone = PhoneNumber.korean("010-1234-5678");
            
            // When
            String formatted = koreanPhone.getFormatted();
            
            // Then
            assertThat(formatted).isEqualTo("010-1234-5678");
        }
        
        @Test
        @DisplayName("국제 전화번호 형식화 성공")
        void getFormatted_WithInternationalPhone_ReturnsFormattedString() {
            // Given
            PhoneNumber internationalPhone = PhoneNumber.international("1234567890");
            
            // When
            String formatted = internationalPhone.getFormatted();
            
            // Then
            assertThat(formatted).isEqualTo("+1234567890");
        }
        
        @Test
        @DisplayName("숫자만 추출 성공")
        void getDigitsOnly_ReturnsOnlyDigits() {
            // Given
            PhoneNumber koreanPhone = PhoneNumber.korean("010-1234-5678");
            PhoneNumber internationalPhone = PhoneNumber.international("+1234567890");
            
            // When
            String koreanDigits = koreanPhone.getDigitsOnly();
            String internationalDigits = internationalPhone.getDigitsOnly();
            
            // Then
            assertThat(koreanDigits).isEqualTo("01012345678");
            assertThat(internationalDigits).isEqualTo("1234567890");
        }
    }
    
    @Nested
    @DisplayName("한국 전화번호 특화 테스트")
    class KoreanPhoneNumberTest {
        
        @Test
        @DisplayName("지역번호 추출 성공")
        void getAreaCode_WithKoreanPhone_ReturnsAreaCode() {
            // Given
            PhoneNumber phoneNumber = PhoneNumber.korean("010-1234-5678");
            
            // When
            String areaCode = phoneNumber.getAreaCode();
            
            // Then
            assertThat(areaCode).isEqualTo("010");
        }
        
        @Test
        @DisplayName("국제 전화번호에서 지역번호 추출 시 null 반환")
        void getAreaCode_WithInternationalPhone_ReturnsNull() {
            // Given
            PhoneNumber phoneNumber = PhoneNumber.international("+1234567890");
            
            // When
            String areaCode = phoneNumber.getAreaCode();
            
            // Then
            assertThat(areaCode).isNull();
        }
    }
    
    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {
        
        @Test
        @DisplayName("같은 전화번호는 동등함")
        void equals_WithSamePhoneNumber_ReturnsTrue() {
            // Given
            PhoneNumber phone1 = PhoneNumber.korean("010-1234-5678");
            PhoneNumber phone2 = PhoneNumber.korean("010-1234-5678");
            
            // When & Then
            assertThat(phone1).isEqualTo(phone2);
            assertThat(phone1.hashCode()).isEqualTo(phone2.hashCode());
        }
        
        @Test
        @DisplayName("다른 전화번호는 동등하지 않음")
        void equals_WithDifferentPhoneNumber_ReturnsFalse() {
            // Given
            PhoneNumber phone1 = PhoneNumber.korean("010-1234-5678");
            PhoneNumber phone2 = PhoneNumber.korean("010-9876-5432");
            
            // When & Then
            assertThat(phone1).isNotEqualTo(phone2);
        }
        
        @Test
        @DisplayName("다른 타입의 전화번호는 동등하지 않음")
        void equals_WithDifferentType_ReturnsFalse() {
            // Given
            PhoneNumber koreanPhone = PhoneNumber.korean("010-1234-5678");
            PhoneNumber internationalPhone = PhoneNumber.international("+821012345678");
            
            // When & Then
            assertThat(koreanPhone).isNotEqualTo(internationalPhone);
        }
    }
    
    @Nested
    @DisplayName("국가 코드 테스트")
    class CountryCodeTest {
        
        @Test
        @DisplayName("한국 전화번호는 한국 국가 코드")
        void isKorean_WithKoreanPhone_ReturnsTrue() {
            // Given
            PhoneNumber phoneNumber = PhoneNumber.korean("010-1234-5678");
            
            // When & Then
            assertThat(phoneNumber.isKorean()).isTrue();
            assertThat(phoneNumber.isInternational()).isFalse();
        }
        
        @Test
        @DisplayName("국제 전화번호는 국제 국가 코드")
        void isInternational_WithInternationalPhone_ReturnsTrue() {
            // Given
            PhoneNumber phoneNumber = PhoneNumber.international("+1234567890");
            
            // When & Then
            assertThat(phoneNumber.isInternational()).isTrue();
            assertThat(phoneNumber.isKorean()).isFalse();
        }
    }
}