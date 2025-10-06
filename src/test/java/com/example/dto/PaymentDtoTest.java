package com.example.dto;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.domain.Payment.PaymentMethod;
import com.example.domain.Payment.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentDto 테스트")
class PaymentDtoTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("Builder를 통한 PaymentDto 생성")
        void createPaymentDto_WithBuilder_Success() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            // When
            PaymentDto dto = PaymentDto.builder()
                    .id(1L)
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(50.00))
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .status(PaymentStatus.PENDING)
                    .transactionId("TXN-001")
                    .failureReason("Test failure")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // Then
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getOrderId()).isEqualTo(100L);
            assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(50.00));
            assertThat(dto.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(dto.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(dto.getTransactionId()).isEqualTo("TXN-001");
            assertThat(dto.getFailureReason()).isEqualTo("Test failure");
            assertThat(dto.getCreatedAt()).isEqualTo(now);
            assertThat(dto.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("기본 생성자를 통한 PaymentDto 생성")
        void createPaymentDto_WithDefaultConstructor_Success() {
            // When
            PaymentDto dto = PaymentDto.builder().build();

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isNull();
            assertThat(dto.getOrderId()).isNull();
            assertThat(dto.getAmount()).isNull();
            assertThat(dto.getPaymentMethod()).isNull();
            assertThat(dto.getStatus()).isNull();
            assertThat(dto.getTransactionId()).isNull();
            assertThat(dto.getFailureReason()).isNull();
            assertThat(dto.getCreatedAt()).isNull();
            assertThat(dto.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Entity 변환 테스트")
    class EntityConversionTest {

        @Test
        @DisplayName("Payment 엔티티에서 PaymentDto로 변환")
        void fromPayment_WithValidPayment_Success() {
            // Given
            Member testMember = Member.builder()
                    .name("Test Member")
                    .email("test@example.com")
                    .phoneNumber("010-1234-5678")
                    .build();
            
            Order testOrder = Order.builder()
                    .orderNumber("ORD-PAYMENT")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();
            
            Payment payment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(PaymentMethod.BANK_TRANSFER)
                    .build();

            // When
            PaymentDto result = PaymentDto.from(payment);

            // Then
            assertThat(result.getId()).isEqualTo(payment.getId());
            assertThat(result.getOrderId()).isEqualTo(payment.getOrder().getId());
            assertThat(result.getAmount()).isEqualTo(payment.getAmount());
            assertThat(result.getPaymentMethod()).isEqualTo(payment.getPaymentMethod());
            assertThat(result.getStatus()).isEqualTo(payment.getStatus());
            assertThat(result.getTransactionId()).isEqualTo(payment.getTransactionId());
            assertThat(result.getFailureReason()).isEqualTo(payment.getFailureReason());
            assertThat(result.getCreatedAt()).isEqualTo(payment.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(payment.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 필드의 Getter 정상 동작")
        void getter_AllFields_Success() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            PaymentDto dto = PaymentDto.builder()
                    .id(100L)
                    .orderId(200L)
                    .amount(BigDecimal.valueOf(150.00))
                    .paymentMethod(PaymentMethod.DEBIT_CARD)
                    .status(PaymentStatus.COMPLETED)
                    .transactionId("TXN-GETTER")
                    .failureReason("Test failure")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // When & Then
            assertThat(dto.getId()).isEqualTo(100L);
            assertThat(dto.getOrderId()).isEqualTo(200L);
            assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(150.00));
            assertThat(dto.getPaymentMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
            assertThat(dto.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(dto.getTransactionId()).isEqualTo("TXN-GETTER");
            assertThat(dto.getFailureReason()).isEqualTo("Test failure");
            assertThat(dto.getCreatedAt()).isEqualTo(now);
            assertThat(dto.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("null 값들에 대한 Getter 정상 동작")
        void getter_NullValues_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder().build();

            // When & Then
            assertThat(dto.getId()).isNull();
            assertThat(dto.getOrderId()).isNull();
            assertThat(dto.getAmount()).isNull();
            assertThat(dto.getPaymentMethod()).isNull();
            assertThat(dto.getStatus()).isNull();
            assertThat(dto.getTransactionId()).isNull();
            assertThat(dto.getFailureReason()).isNull();
            assertThat(dto.getCreatedAt()).isNull();
            assertThat(dto.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Equals/HashCode 테스트")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("동일한 필드를 가진 PaymentDto는 equals true")
        void equals_SameFields_True() {
            // Given
            PaymentDto dto1 = PaymentDto.builder()
                    .id(1L)
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(50.00))
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .status(PaymentStatus.PENDING)
                    .transactionId("TXN-EQUAL")
                    .build();

            PaymentDto dto2 = PaymentDto.builder()
                    .id(1L)
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(50.00))
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .status(PaymentStatus.PENDING)
                    .transactionId("TXN-EQUAL")
                    .build();

            // When & Then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 PaymentDto는 equals false")
        void equals_DifferentFields_False() {
            // Given
            PaymentDto dto1 = PaymentDto.builder()
                    .id(1L)
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(50.00))
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .status(PaymentStatus.PENDING)
                    .build();

            PaymentDto dto2 = PaymentDto.builder()
                    .id(2L)
                    .orderId(200L)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(PaymentMethod.BANK_TRANSFER)
                    .status(PaymentStatus.COMPLETED)
                    .build();

            // When & Then
            assertThat(dto1).isNotEqualTo(dto2);
        }

        @Test
        @DisplayName("null과의 equals 비교")
        void equals_Null_False() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .id(1L)
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(50.00))
                    .build();

            // When & Then
            assertThat(dto).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과의 equals 비교")
        void equals_DifferentType_False() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .id(1L)
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(50.00))
                    .build();

            // When & Then
            assertThat(dto).isNotEqualTo("Not a PaymentDto");
        }

        @Test
        @DisplayName("자기 자신과의 equals 비교")
        void equals_Self_True() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .id(1L)
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(50.00))
                    .build();

            // When & Then
            assertThat(dto).isEqualTo(dto);
        }
    }

    @Nested
    @DisplayName("ToString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("ToString 메서드 정상 동작")
        void toString_ValidDto_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .id(1L)
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(75.50))
                    .paymentMethod(PaymentMethod.CASH)
                    .status(PaymentStatus.COMPLETED)
                    .transactionId("TXN-TOSTRING")
                    .build();

            // When
            String result = dto.toString();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("PaymentDto");
            assertThat(result).contains("TXN-TOSTRING");
            assertThat(result).contains("CASH");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @ParameterizedTest
        @ValueSource(doubles = {-1.0, -0.01})
        @DisplayName("금액이 음수인 경우")
        void validation_NegativeAmount(double amount) {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .orderId(100L)
                    .amount(BigDecimal.valueOf(amount))
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .status(PaymentStatus.PENDING)
                    .build();

            // When & Then
            // Note: PaymentDto doesn't have validation logic, this is just for testing getter
            assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(amount));
        }

        @Test
        @DisplayName("주문 ID가 null인 경우")
        void validation_NullOrderId_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .orderId(null)
                    .amount(BigDecimal.valueOf(50.00))
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .status(PaymentStatus.PENDING)
                    .build();

            // When & Then
            assertThat(dto.getOrderId()).isNull();
        }
    }

    @Nested
    @DisplayName("상태 관련 테스트")
    class StatusTest {

        @Test
        @DisplayName("PENDING 상태 확인")
        void status_Pending_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .status(PaymentStatus.PENDING)
                    .build();

            // When & Then
            assertThat(dto.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("COMPLETED 상태 확인")
        void status_Completed_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .status(PaymentStatus.COMPLETED)
                    .build();

            // When & Then
            assertThat(dto.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("FAILED 상태 확인")
        void status_Failed_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .status(PaymentStatus.FAILED)
                    .build();

            // When & Then
            assertThat(dto.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("REFUNDED 상태 확인")
        void status_Refunded_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .status(PaymentStatus.REFUNDED)
                    .build();

            // When & Then
            assertThat(dto.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }
    }

    @Nested
    @DisplayName("결제 방법 관련 테스트")
    class PaymentMethodTest {

        @Test
        @DisplayName("CREDIT_CARD 결제 방법 확인")
        void paymentMethod_CreditCard_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .paymentMethod(PaymentMethod.CREDIT_CARD)
                    .build();

            // When & Then
            assertThat(dto.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        }

        @Test
        @DisplayName("DEBIT_CARD 결제 방법 확인")
        void paymentMethod_DebitCard_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .paymentMethod(PaymentMethod.DEBIT_CARD)
                    .build();

            // When & Then
            assertThat(dto.getPaymentMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
        }

        @Test
        @DisplayName("BANK_TRANSFER 결제 방법 확인")
        void paymentMethod_BankTransfer_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .paymentMethod(PaymentMethod.BANK_TRANSFER)
                    .build();

            // When & Then
            assertThat(dto.getPaymentMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        }

        @Test
        @DisplayName("CASH 결제 방법 확인")
        void paymentMethod_Cash_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .paymentMethod(PaymentMethod.CASH)
                    .build();

            // When & Then
            assertThat(dto.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
        }
    }

    @Nested
    @DisplayName("거래 ID 관련 테스트")
    class TransactionIdTest {

        @Test
        @DisplayName("거래 ID 설정 및 조회")
        void transactionId_SetAndGet_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .transactionId("TXN-12345")
                    .build();

            // When & Then
            assertThat(dto.getTransactionId()).isEqualTo("TXN-12345");
        }

        @Test
        @DisplayName("거래 ID가 null인 경우")
        void transactionId_Null_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .transactionId(null)
                    .build();

            // When & Then
            assertThat(dto.getTransactionId()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("거래 ID가 null이거나 빈 문자열인 경우")
        void transactionId_NullOrEmpty_Success(String transactionId) {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .transactionId(transactionId)
                    .build();

            // When & Then
            assertThat(dto.getTransactionId()).isEqualTo(transactionId);
        }
    }

    @Nested
    @DisplayName("실패 사유 관련 테스트")
    class FailureReasonTest {

        @Test
        @DisplayName("실패 사유 설정 및 조회")
        void failureReason_SetAndGet_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .failureReason("Insufficient funds")
                    .build();

            // When & Then
            assertThat(dto.getFailureReason()).isEqualTo("Insufficient funds");
        }

        @Test
        @DisplayName("실패 사유가 null인 경우")
        void failureReason_Null_Success() {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .failureReason(null)
                    .build();

            // When & Then
            assertThat(dto.getFailureReason()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("실패 사유가 null이거나 빈 문자열인 경우")
        void failureReason_NullOrEmpty_Success(String failureReason) {
            // Given
            PaymentDto dto = PaymentDto.builder()
                    .failureReason(failureReason)
                    .build();

            // When & Then
            assertThat(dto.getFailureReason()).isEqualTo(failureReason);
        }
    }

    @Nested
    @DisplayName("시간 관련 테스트")
    class TimeTest {

        @Test
        @DisplayName("생성 시간 설정 및 조회")
        void createdAt_SetAndGet_Success() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            PaymentDto dto = PaymentDto.builder()
                    .createdAt(now)
                    .build();

            // When & Then
            assertThat(dto.getCreatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("수정 시간 설정 및 조회")
        void updatedAt_SetAndGet_Success() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            PaymentDto dto = PaymentDto.builder()
                    .updatedAt(now)
                    .build();

            // When & Then
            assertThat(dto.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("생성 시간과 수정 시간이 다른 경우")
        void time_DifferentCreatedAndUpdated_Success() {
            // Given
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 11, 0);

            PaymentDto dto = PaymentDto.builder()
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When & Then
            assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
            assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(dto.getCreatedAt()).isNotEqualTo(dto.getUpdatedAt());
        }
    }
}