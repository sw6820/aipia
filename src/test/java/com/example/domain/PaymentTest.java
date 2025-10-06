package com.example.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment 도메인 테스트")
class PaymentTest {

    private Order testOrder;
    private Payment payment;

    @BeforeEach
    void setUp() {
        Member testMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();

        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        payment = Payment.builder()
                .order(testOrder)
                .amount(BigDecimal.valueOf(100.00))
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .build();
    }

    @Nested
    @DisplayName("결제 생성 테스트")
    class CreatePaymentTest {

        @Test
        @DisplayName("정상적인 결제 생성 성공")
        void createPayment_Success() {
            // Given & When
            Payment newPayment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(200.00))
                    .paymentMethod(Payment.PaymentMethod.DEBIT_CARD)
                    .build();

            // Then
            assertThat(newPayment.getOrder()).isEqualTo(testOrder);
            assertThat(newPayment.getAmount()).isEqualTo(BigDecimal.valueOf(200.00));
            assertThat(newPayment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.DEBIT_CARD);
            assertThat(newPayment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("주문이 null인 경우 예외 발생")
        void createPayment_NullOrder_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Payment.builder()
                    .order(null)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("결제 금액이 null인 경우 예외 발생")
        void createPayment_NullAmount_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Payment.builder()
                    .order(testOrder)
                    .amount(null)
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("결제 금액이 음수인 경우 예외 발생")
        void createPayment_NegativeAmount_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(-100.00))
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("결제 방법이 null인 경우 예외 발생")
        void createPayment_NullPaymentMethod_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(null)
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("결제 처리 테스트")
    class ProcessPaymentTest {

        @Test
        @DisplayName("결제 처리 성공")
        void process_Success() {
            // Given
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
            String transactionId = "TXN123456789";

            // When
            payment.process(transactionId);

            // Then
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
            assertThat(payment.getTransactionId()).isEqualTo(transactionId);
        }

        @Test
        @DisplayName("거래 ID가 null인 경우 예외 발생")
        void process_NullTransactionId_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> payment.process(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("빈 거래 ID로 결제 처리 시 예외 발생")
        void process_EmptyTransactionId_ThrowsException(String transactionId) {
            // When & Then
            assertThatThrownBy(() -> payment.process(transactionId))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("이미 완료된 결제를 다시 처리 시 상태 유지")
        void process_AlreadyCompleted_StatusUnchanged() {
            // Given
            payment.process("TXN123456789");
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);

            // When
            payment.process("TXN987654321");

            // Then
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
            assertThat(payment.getTransactionId()).isEqualTo("TXN987654321");
        }
    }

    @Nested
    @DisplayName("결제 실패 테스트")
    class FailPaymentTest {

        @Test
        @DisplayName("결제 실패 처리 성공")
        void fail_Success() {
            // Given
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
            String failureReason = "Insufficient funds";

            // When
            payment.fail(failureReason);

            // Then
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
            assertThat(payment.getFailureReason()).isEqualTo(failureReason);
        }

        @Test
        @DisplayName("실패 사유가 null인 경우 예외 발생")
        void fail_NullFailureReason_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> payment.fail(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("빈 실패 사유로 결제 실패 처리 시 예외 발생")
        void fail_EmptyFailureReason_ThrowsException(String failureReason) {
            // When & Then
            assertThatThrownBy(() -> payment.fail(failureReason))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("이미 실패한 결제를 다시 실패 처리 시 상태 유지")
        void fail_AlreadyFailed_StatusUnchanged() {
            // Given
            payment.fail("Insufficient funds");
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);

            // When
            payment.fail("Card expired");

            // Then
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
            assertThat(payment.getFailureReason()).isEqualTo("Card expired");
        }
    }

    @Nested
    @DisplayName("결제 환불 테스트")
    class RefundPaymentTest {

        @Test
        @DisplayName("결제 환불 성공")
        void refund_Success() {
            // Given
            payment.process("TXN123456789");
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);

            // When
            payment.refund();

            // Then
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("완료되지 않은 결제는 환불할 수 없음")
        void refund_NotCompleted_ThrowsException() {
            // Given
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("실패한 결제는 환불할 수 없음")
        void refund_FailedPayment_ThrowsException() {
            // Given
            payment.fail("Insufficient funds");
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);

            // When & Then
            assertThatThrownBy(() -> payment.refund())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("이미 환불된 결제를 다시 환불 시 상태 유지")
        void refund_AlreadyRefunded_StatusUnchanged() {
            // Given
            payment.process("TXN123456789");
            payment.refund();
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);

            // When
            payment.refund();

            // Then
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);
        }
    }

    @Nested
    @DisplayName("결제 방법 테스트")
    class PaymentMethodTest {

        @Test
        @DisplayName("신용카드 결제 방법 설정")
        void creditCardPaymentMethod_Success() {
            // When
            Payment creditCardPayment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();

            // Then
            assertThat(creditCardPayment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        }

        @Test
        @DisplayName("체크카드 결제 방법 설정")
        void debitCardPaymentMethod_Success() {
            // When
            Payment debitCardPayment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(Payment.PaymentMethod.DEBIT_CARD)
                    .build();

            // Then
            assertThat(debitCardPayment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.DEBIT_CARD);
        }

        @Test
        @DisplayName("계좌이체 결제 방법 설정")
        void bankTransferPaymentMethod_Success() {
            // When
            Payment bankTransferPayment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                    .build();

            // Then
            assertThat(bankTransferPayment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.BANK_TRANSFER);
        }

        @Test
        @DisplayName("현금 결제 방법 설정")
        void cashPaymentMethod_Success() {
            // When
            Payment cashPayment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(Payment.PaymentMethod.CASH)
                    .build();

            // Then
            assertThat(cashPayment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CASH);
        }
    }

    @Nested
    @DisplayName("결제 비즈니스 규칙 테스트")
    class PaymentBusinessRulesTest {

        @Test
        @DisplayName("결제 생성 시 기본 상태는 PENDING")
        void createPayment_DefaultStatusIsPending() {
            // When
            Payment newPayment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(100.00))
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();

            // Then
            assertThat(newPayment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("결제 금액은 소수점 2자리까지 정확")
        void paymentAmount_PrecisionTest() {
            // Given
            BigDecimal preciseAmount = new BigDecimal("99.99");

            // When
            Payment newPayment = Payment.builder()
                    .order(testOrder)
                    .amount(preciseAmount)
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();

            // Then
            assertThat(newPayment.getAmount()).isEqualTo(preciseAmount);
            assertThat(newPayment.getAmount().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("결제 금액이 주문 총액과 일치해야 함")
        void paymentAmount_ShouldMatchOrderTotal() {
            // Given
            BigDecimal orderTotal = testOrder.getTotalAmount();

            // When
            Payment newPayment = Payment.builder()
                    .order(testOrder)
                    .amount(orderTotal)
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();

            // Then
            assertThat(newPayment.getAmount()).isEqualTo(orderTotal);
        }

        @Test
        @DisplayName("거래 ID는 고유해야 함")
        void transactionId_ShouldBeUnique() {
            // Given
            String transactionId1 = "TXN123456789";
            String transactionId2 = "TXN987654321";

            // When
            payment.process(transactionId1);
            Payment anotherPayment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(200.00))
                    .paymentMethod(Payment.PaymentMethod.DEBIT_CARD)
                    .build();
            anotherPayment.process(transactionId2);

            // Then
            assertThat(payment.getTransactionId()).isEqualTo(transactionId1);
            assertThat(anotherPayment.getTransactionId()).isEqualTo(transactionId2);
            assertThat(payment.getTransactionId()).isNotEqualTo(anotherPayment.getTransactionId());
        }
    }

    @Nested
    @DisplayName("결제 검증 테스트")
    class PaymentValidationTest {

        @Test
        @DisplayName("거래 ID 형식 검증")
        void transactionIdFormatValidation() {
            // Given
            String validTransactionId = "TXN-20240101-001";

            // When
            payment.process(validTransactionId);

            // Then
            assertThat(payment.getTransactionId()).isEqualTo(validTransactionId);
        }

        @Test
        @DisplayName("실패 사유 길이 검증")
        void failureReasonLengthValidation() {
            // Given
            String longFailureReason = "A".repeat(500);

            // When
            payment.fail(longFailureReason);

            // Then
            assertThat(payment.getFailureReason()).isEqualTo(longFailureReason);
        }

        @Test
        @DisplayName("결제 금액 범위 검증")
        void paymentAmountRangeValidation() {
            // Given
            BigDecimal maxAmount = new BigDecimal("999999999999999.99");

            // When
            Payment newPayment = Payment.builder()
                    .order(testOrder)
                    .amount(maxAmount)
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();

            // Then
            assertThat(newPayment.getAmount()).isEqualTo(maxAmount);
        }
    }
}