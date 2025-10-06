package com.example.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object 테스트")
class MoneyTest {
    
    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {
        
        @Test
        @DisplayName("유효한 금액과 통화로 생성 성공")
        void createWithValidAmountAndCurrency_Success() {
            // Given
            BigDecimal amount = BigDecimal.valueOf(1000.50);
            Currency currency = Currency.getInstance("USD");
            
            // When
            Money money = new Money(amount, currency);
            
            // Then
            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.50));
            assertThat(money.getCurrency()).isEqualTo("USD");
        }
        
        @Test
        @DisplayName("USD 팩토리 메서드로 생성 성공")
        void createWithUsdFactory_Success() {
            // Given
            BigDecimal amount = BigDecimal.valueOf(100.00);
            
            // When
            Money money = Money.usd(amount);
            
            // Then
            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
            assertThat(money.getCurrency()).isEqualTo("USD");
        }
        
        @Test
        @DisplayName("KRW 팩토리 메서드로 생성 성공")
        void createWithKrwFactory_Success() {
            // Given
            BigDecimal amount = BigDecimal.valueOf(100000);
            
            // When
            Money money = Money.krw(amount);
            
            // Then
            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
            assertThat(money.getCurrency()).isEqualTo("KRW");
        }
        
        @Test
        @DisplayName("문자열 금액으로 생성 성공")
        void createWithStringAmount_Success() {
            // Given
            String amount = "150.75";
            String currencyCode = "USD";
            
            // When
            Money money = Money.of(amount, currencyCode);
            
            // Then
            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.75));
            assertThat(money.getCurrency()).isEqualTo("USD");
        }
        
        @Test
        @DisplayName("음수 금액으로 생성 실패")
        void createWithNegativeAmount_ThrowsException() {
            // Given
            BigDecimal negativeAmount = BigDecimal.valueOf(-100);
            Currency currency = Currency.getInstance("USD");
            
            // When & Then
            assertThatThrownBy(() -> new Money(negativeAmount, currency))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount cannot be negative");
        }
        
        @Test
        @DisplayName("null 금액으로 생성 실패")
        void createWithNullAmount_ThrowsException() {
            // Given
            Currency currency = Currency.getInstance("USD");
            
            // When & Then
            assertThatThrownBy(() -> new Money(null, currency))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount cannot be null");
        }
        
        @Test
        @DisplayName("null 통화로 생성 실패")
        void createWithNullCurrency_ThrowsException() {
            // Given
            BigDecimal amount = BigDecimal.valueOf(100);
            
            // When & Then
            assertThatThrownBy(() -> new Money(amount, (Currency) null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency cannot be null");
        }
    }
    
    @Nested
    @DisplayName("산술 연산 테스트")
    class ArithmeticTest {
        
        @Test
        @DisplayName("같은 통화 금액 더하기 성공")
        void add_WithSameCurrency_Success() {
            // Given
            Money money1 = Money.usd(BigDecimal.valueOf(100));
            Money money2 = Money.usd(BigDecimal.valueOf(50));
            
            // When
            Money result = money1.add(money2);
            
            // Then
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
            assertThat(result.getCurrency()).isEqualTo("USD");
        }
        
        @Test
        @DisplayName("다른 통화 금액 더하기 실패")
        void add_WithDifferentCurrency_ThrowsException() {
            // Given
            Money usdMoney = Money.usd(BigDecimal.valueOf(100));
            Money krwMoney = Money.krw(BigDecimal.valueOf(100000));
            
            // When & Then
            assertThatThrownBy(() -> usdMoney.add(krwMoney))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot perform operation on different currencies");
        }
        
        @Test
        @DisplayName("같은 통화 금액 빼기 성공")
        void subtract_WithSameCurrency_Success() {
            // Given
            Money money1 = Money.usd(BigDecimal.valueOf(150));
            Money money2 = Money.usd(BigDecimal.valueOf(50));
            
            // When
            Money result = money1.subtract(money2);
            
            // Then
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(result.getCurrency()).isEqualTo("USD");
        }
        
        @Test
        @DisplayName("음수 결과가 되는 빼기 실패")
        void subtract_WithNegativeResult_ThrowsException() {
            // Given
            Money money1 = Money.usd(BigDecimal.valueOf(50));
            Money money2 = Money.usd(BigDecimal.valueOf(100));
            
            // When & Then
            assertThatThrownBy(() -> money1.subtract(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Result cannot be negative");
        }
        
        @Test
        @DisplayName("금액 곱하기 성공")
        void multiply_WithValidFactor_Success() {
            // Given
            Money money = Money.usd(BigDecimal.valueOf(100));
            BigDecimal factor = BigDecimal.valueOf(1.5);
            
            // When
            Money result = money.multiply(factor);
            
            // Then
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
            assertThat(result.getCurrency()).isEqualTo("USD");
        }
        
        @Test
        @DisplayName("null 인수로 곱하기 실패")
        void multiply_WithNullFactor_ThrowsException() {
            // Given
            Money money = Money.usd(BigDecimal.valueOf(100));
            
            // When & Then
            assertThatThrownBy(() -> money.multiply(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Factor cannot be null");
        }
    }
    
    @Nested
    @DisplayName("비교 테스트")
    class ComparisonTest {
        
        @Test
        @DisplayName("금액 크기 비교 성공")
        void isGreaterThan_WithValidComparison_ReturnsCorrectResult() {
            // Given
            Money money1 = Money.usd(BigDecimal.valueOf(150));
            Money money2 = Money.usd(BigDecimal.valueOf(100));
            
            // When & Then
            assertThat(money1.isGreaterThan(money2)).isTrue();
            assertThat(money2.isGreaterThan(money1)).isFalse();
        }
        
        @Test
        @DisplayName("금액 크기 이상 비교 성공")
        void isGreaterThanOrEqual_WithValidComparison_ReturnsCorrectResult() {
            // Given
            Money money1 = Money.usd(BigDecimal.valueOf(100));
            Money money2 = Money.usd(BigDecimal.valueOf(100));
            Money money3 = Money.usd(BigDecimal.valueOf(150));
            
            // When & Then
            assertThat(money1.isGreaterThanOrEqual(money2)).isTrue();
            assertThat(money1.isGreaterThanOrEqual(money3)).isFalse();
            assertThat(money3.isGreaterThanOrEqual(money1)).isTrue();
        }
        
        @Test
        @DisplayName("0 금액 확인 성공")
        void isZero_WithZeroAmount_ReturnsTrue() {
            // Given
            Money zeroMoney = Money.usd(BigDecimal.ZERO);
            Money nonZeroMoney = Money.usd(BigDecimal.valueOf(100));
            
            // When & Then
            assertThat(zeroMoney.isZero()).isTrue();
            assertThat(nonZeroMoney.isZero()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {
        
        @Test
        @DisplayName("같은 금액과 통화는 동등함")
        void equals_WithSameAmountAndCurrency_ReturnsTrue() {
            // Given
            Money money1 = Money.usd(BigDecimal.valueOf(100));
            Money money2 = Money.usd(BigDecimal.valueOf(100));
            
            // When & Then
            assertThat(money1).isEqualTo(money2);
            assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
        }
        
        @Test
        @DisplayName("다른 금액은 동등하지 않음")
        void equals_WithDifferentAmount_ReturnsFalse() {
            // Given
            Money money1 = Money.usd(BigDecimal.valueOf(100));
            Money money2 = Money.usd(BigDecimal.valueOf(150));
            
            // When & Then
            assertThat(money1).isNotEqualTo(money2);
        }
        
        @Test
        @DisplayName("다른 통화는 동등하지 않음")
        void equals_WithDifferentCurrency_ReturnsFalse() {
            // Given
            Money usdMoney = Money.usd(BigDecimal.valueOf(100));
            Money krwMoney = Money.krw(BigDecimal.valueOf(100));
            
            // When & Then
            assertThat(usdMoney).isNotEqualTo(krwMoney);
        }
    }
    
    @Nested
    @DisplayName("문자열 표현 테스트")
    class StringRepresentationTest {
        
        @Test
        @DisplayName("문자열 표현 형식 확인")
        void toString_ReturnsCorrectFormat() {
            // Given
            Money money = Money.usd(BigDecimal.valueOf(123.45));
            
            // When
            String stringRepresentation = money.toString();
            
            // Then
            assertThat(stringRepresentation).isEqualTo("123.45 USD");
        }
    }
}