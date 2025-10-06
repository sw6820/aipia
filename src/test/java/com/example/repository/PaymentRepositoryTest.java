package com.example.repository;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.domain.Payment.PaymentMethod;
import com.example.domain.Payment.PaymentStatus;
import com.example.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * PaymentRepository 통합 테스트
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PaymentRepository 테스트")
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private Member testMember;
    private Order testOrder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // 테스트 멤버 생성
        testMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        testMember = entityManager.persistAndFlush(testMember);

        // 테스트 주문 생성
        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        testOrder = entityManager.persistAndFlush(testOrder);

        // 테스트 결제 생성
        testPayment = Payment.builder()
                .order(testOrder)
                .amount(BigDecimal.valueOf(100.00))
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .build();
        testPayment = entityManager.persistAndFlush(testPayment);
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("결제 저장 성공")
        void save_Payment_Success() {
            // Given
            Order newOrder = Order.builder()
                    .orderNumber("ORD-NEW")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(50.00))
                    .build();
            newOrder = entityManager.persistAndFlush(newOrder);
            
            Payment newPayment = Payment.builder()
                    .order(newOrder)
                    .amount(BigDecimal.valueOf(50.00))
                    .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                    .build();

            // When
            Payment savedPayment = paymentRepository.save(newPayment);
            entityManager.flush();

            // Then
            assertThat(savedPayment.getId()).isNotNull();
            assertThat(savedPayment.getAmount()).isEqualTo(BigDecimal.valueOf(50.00));
            assertThat(savedPayment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.BANK_TRANSFER);
            assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("결제 조회 성공")
        void findById_Payment_Success() {
            // When
            Optional<Payment> foundPayment = paymentRepository.findById(testPayment.getId());

            // Then
            assertThat(foundPayment).isPresent();
            assertThat(foundPayment.get().getId()).isEqualTo(testPayment.getId());
            assertThat(foundPayment.get().getAmount()).isEqualTo(testPayment.getAmount());
            assertThat(foundPayment.get().getPaymentMethod()).isEqualTo(testPayment.getPaymentMethod());
            assertThat(foundPayment.get().getStatus()).isEqualTo(testPayment.getStatus());
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 빈 Optional 반환")
        void findById_NonExistentPayment_ReturnsEmpty() {
            // When
            Optional<Payment> foundPayment = paymentRepository.findById(999L);

            // Then
            assertThat(foundPayment).isEmpty();
        }

        @Test
        @DisplayName("모든 결제 조회 성공")
        void findAll_Payments_Success() {
            // Given
            Order anotherOrder = Order.builder()
                    .orderNumber("ORD-ANOTHER")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            anotherOrder = entityManager.persistAndFlush(anotherOrder);
            
            Payment anotherPayment = Payment.builder()
                    .order(anotherOrder)
                    .amount(BigDecimal.valueOf(200.00))
                    .paymentMethod(Payment.PaymentMethod.CASH)
                    .build();
            entityManager.persistAndFlush(anotherPayment);

            // When
            List<Payment> allPayments = paymentRepository.findAll();

            // Then
            assertThat(allPayments).hasSize(2);
            assertThat(allPayments).contains(testPayment, anotherPayment);
        }

        @Test
        @DisplayName("결제 수정 성공")
        void update_Payment_Success() {
            // Given
            testPayment.process("TXN-123456");

            // When
            Payment updatedPayment = paymentRepository.save(testPayment);
            entityManager.flush();

            // Then
            assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(updatedPayment.getTransactionId()).isEqualTo("TXN-123456");
        }

        @Test
        @DisplayName("결제 삭제 성공")
        void delete_Payment_Success() {
            // When
            paymentRepository.delete(testPayment);
            entityManager.flush();

            // Then
            Optional<Payment> deletedPayment = paymentRepository.findById(testPayment.getId());
            assertThat(deletedPayment).isEmpty();
        }
    }

    @Nested
    @DisplayName("주문별 결제 조회 테스트")
    class OrderBasedQueryTest {

        @Test
        @DisplayName("주문 ID로 결제 조회 성공")
        void findByOrderId_WithValidOrderId_Success() {
            // When
            Optional<Payment> payment = paymentRepository.findByOrderId(testOrder.getId());

            // Then
            assertThat(payment).isPresent();
            assertThat(payment.get().getOrder().getId()).isEqualTo(testOrder.getId());
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 결제 조회 시 빈 Optional 반환")
        void findByOrderId_WithNonExistentOrderId_ReturnsEmpty() {
            // When
            Optional<Payment> payment = paymentRepository.findByOrderId(999L);

            // Then
            assertThat(payment).isEmpty();
        }

        @Test
        @DisplayName("주문 ID로 결제 조회 시 첫 번째 결제 반환")
        void findByOrderId_WithMultiplePayments_Success() {
            // Given
            Order secondOrder = Order.builder()
                    .orderNumber("ORD-SECOND")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(50.00))
                    .build();
            secondOrder = entityManager.persistAndFlush(secondOrder);
            
            Payment secondPayment = Payment.builder()
                    .order(secondOrder)
                    .amount(BigDecimal.valueOf(50.00))
                    .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                    .build();
            entityManager.persistAndFlush(secondPayment);

            // When
            Optional<Payment> payment = paymentRepository.findByOrderId(secondOrder.getId());

            // Then
            assertThat(payment).isPresent();
            assertThat(payment.get().getOrder().getId()).isEqualTo(secondOrder.getId());
        }
    }

    @Nested
    @DisplayName("상태별 결제 조회 테스트")
    class StatusBasedQueryTest {

        @Test
        @DisplayName("상태로 결제 조회 성공")
        void findByStatus_WithValidStatus_Success() {
            // When
            List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.PENDING);

            // Then
            assertThat(payments).hasSize(1);
            assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("존재하지 않는 상태로 결제 조회 시 빈 리스트 반환")
        void findByStatus_WithNonExistentStatus_ReturnsEmpty() {
            // When
            List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.REFUNDED);

            // Then
            assertThat(payments).isEmpty();
        }

        @Test
        @DisplayName("상태로 결제 조회 시 여러 결제 반환")
        void findByStatus_WithMultiplePayments_Success() {
            // Given
            Order anotherOrder = Order.builder()
                    .orderNumber("ORD-ANOTHER-PENDING")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(75.00))
                    .build();
            anotherOrder = entityManager.persistAndFlush(anotherOrder);
            
            Payment anotherPendingPayment = Payment.builder()
                    .order(anotherOrder)
                    .amount(BigDecimal.valueOf(75.00))
                    .paymentMethod(Payment.PaymentMethod.CASH)
                    .build();
            entityManager.persistAndFlush(anotherPendingPayment);

            // When
            List<Payment> payments = paymentRepository.findByStatus(PaymentStatus.PENDING);

            // Then
            assertThat(payments).hasSize(2);
            assertThat(payments).allMatch(payment -> payment.getStatus() == PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("거래 ID별 결제 조회 테스트")
    class TransactionIdBasedQueryTest {

        @Test
        @DisplayName("거래 ID로 결제 조회 성공")
        void findByTransactionId_WithValidTransactionId_Success() {
            // Given
            testPayment.process("TXN-123456");
            entityManager.persistAndFlush(testPayment);

            // When
            Optional<Payment> payment = paymentRepository.findByTransactionId("TXN-123456");

            // Then
            assertThat(payment).isPresent();
            assertThat(payment.get().getTransactionId()).isEqualTo("TXN-123456");
        }

        @Test
        @DisplayName("존재하지 않는 거래 ID로 결제 조회 시 빈 Optional 반환")
        void findByTransactionId_WithNonExistentTransactionId_ReturnsEmpty() {
            // When
            Optional<Payment> payment = paymentRepository.findByTransactionId("TXN-NONEXISTENT");

            // Then
            assertThat(payment).isEmpty();
        }

        @Test
        @DisplayName("null 거래 ID로 결제 조회 시 null transaction_id를 가진 결제 반환")
        void findByTransactionId_WithNullTransactionId_ReturnsPaymentWithNullTransactionId() {
            // When
            Optional<Payment> payment = paymentRepository.findByTransactionId(null);

            // Then
            assertThat(payment).isPresent();
            assertThat(payment.get().getTransactionId()).isNull();
        }
    }


    @Nested
    @DisplayName("Fetch Join 테스트")
    class FetchJoinTest {

        @Test
        @DisplayName("주문과 함께 결제 조회 성공")
        void findByIdWithOrder_WithValidId_Success() {
            // When
            Optional<Payment> payment = paymentRepository.findByIdWithOrder(testPayment.getId());

            // Then
            assertThat(payment).isPresent();
            assertThat(payment.get().getOrder()).isNotNull();
            assertThat(payment.get().getOrder().getId()).isEqualTo(testOrder.getId());
            assertThat(payment.get().getOrder().getMember()).isNotNull();
            assertThat(payment.get().getOrder().getMember().getId()).isEqualTo(testMember.getId());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 주문과 함께 결제 조회 시 빈 Optional 반환")
        void findByIdWithOrder_WithNonExistentId_ReturnsEmpty() {
            // When
            Optional<Payment> payment = paymentRepository.findByIdWithOrder(999L);

            // Then
            assertThat(payment).isEmpty();
        }

    }




}