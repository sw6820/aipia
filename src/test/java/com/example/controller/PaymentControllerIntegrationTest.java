package com.example.controller;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.infrastructure.persistence.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PaymentController 통합 테스트")
class PaymentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Member testMember;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        testMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        testMember = memberRepository.save(testMember);

        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @Nested
    @DisplayName("결제 생성 API 테스트")
    class CreatePaymentTest {

        @Test
        @DisplayName("정상적인 결제 생성 성공")
        void createPayment_Success() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.CreatePaymentRequest();
            request.setOrderId(testOrder.getId());
            request.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                    .andExpect(jsonPath("$.amount").value(100.00))
                    .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("존재하지 않는 주문으로 결제 생성 실패")
        void createPayment_OrderNotFound_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.CreatePaymentRequest();
            request.setOrderId(999L);
            request.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("null 주문 ID로 결제 생성 실패")
        void createPayment_NullOrderId_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.CreatePaymentRequest();
            request.setOrderId(null);
            request.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("null 결제 방법으로 결제 생성 실패")
        void createPayment_NullPaymentMethod_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.CreatePaymentRequest();
            request.setOrderId(testOrder.getId());
            request.setPaymentMethod(null);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("다양한 결제 방법으로 결제 생성 성공")
        void createPayment_DifferentPaymentMethods_Success() throws Exception {
            // Given
            var debitCardRequest = new com.example.infrastructure.web.PaymentController.CreatePaymentRequest();
            debitCardRequest.setOrderId(testOrder.getId());
            debitCardRequest.setPaymentMethod(Payment.PaymentMethod.DEBIT_CARD);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(debitCardRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.paymentMethod").value("DEBIT_CARD"));
        }
    }

    @Nested
    @DisplayName("결제 조회 API 테스트")
    class GetPaymentTest {

        private Payment testPayment;

        @BeforeEach
        void setUp() {
            testPayment = Payment.builder()
                    .order(testOrder)
                    .amount(testOrder.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            testPayment = paymentRepository.save(testPayment);
        }

        @Test
        @DisplayName("ID로 결제 조회 성공")
        void getPaymentById_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/{id}", testPayment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testPayment.getId()))
                    .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                    .andExpect(jsonPath("$.amount").value(100.00))
                    .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회")
        void getPaymentById_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/{id}", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("주문 ID로 결제 조회 성공")
        void getPaymentByOrderId_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/order/{orderId}", testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(testOrder.getId()))
                    .andExpect(jsonPath("$.amount").value(100.00));
        }

        @Test
        @DisplayName("존재하지 않는 주문의 결제 조회")
        void getPaymentByOrderId_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/order/{orderId}", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("거래 ID로 결제 조회 성공")
        void getPaymentByTransactionId_Success() throws Exception {
            // Given
            testPayment.process("TXN123456789");
            testPayment = paymentRepository.save(testPayment);

            // When & Then
            mockMvc.perform(get("/api/payments/transaction/{transactionId}", "TXN123456789"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId").value("TXN123456789"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("존재하지 않는 거래 ID로 조회")
        void getPaymentByTransactionId_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/transaction/{transactionId}", "TXN-NONEXISTENT"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("결제 처리 API 테스트")
    class ProcessPaymentTest {

        private Payment testPayment;

        @BeforeEach
        void setUp() {
            testPayment = Payment.builder()
                    .order(testOrder)
                    .amount(testOrder.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            testPayment = paymentRepository.save(testPayment);
        }

        @Test
        @DisplayName("결제 처리 성공")
        void processPayment_Success() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/payments/{id}/process", testPayment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.transactionId").isNotEmpty());
        }

        @Test
        @DisplayName("존재하지 않는 결제 처리 실패")
        void processPayment_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/payments/{id}/process", 999L))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("이미 완료된 결제 재처리 성공")
        void processPayment_AlreadyCompleted_Success() throws Exception {
            // Given
            testPayment.process("TXN123456789");
            testPayment = paymentRepository.save(testPayment);

            // When & Then
            mockMvc.perform(put("/api/payments/{id}/process", testPayment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }
    }

    @Nested
    @DisplayName("결제 실패 API 테스트")
    class FailPaymentTest {

        private Payment testPayment;

        @BeforeEach
        void setUp() {
            testPayment = Payment.builder()
                    .order(testOrder)
                    .amount(testOrder.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            testPayment = paymentRepository.save(testPayment);
        }

        @Test
        @DisplayName("결제 실패 처리 성공")
        void failPayment_Success() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.FailPaymentRequest();
            request.setFailureReason("Insufficient funds");

            // When & Then
            mockMvc.perform(put("/api/payments/{id}/fail", testPayment.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FAILED"))
                    .andExpect(jsonPath("$.failureReason").value("Insufficient funds"));
        }

        @Test
        @DisplayName("빈 실패 사유로 결제 실패 처리 실패")
        void failPayment_EmptyFailureReason_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.FailPaymentRequest();
            request.setFailureReason("");

            // When & Then
            mockMvc.perform(put("/api/payments/{id}/fail", testPayment.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("null 실패 사유로 결제 실패 처리 실패")
        void failPayment_NullFailureReason_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.FailPaymentRequest();
            request.setFailureReason(null);

            // When & Then
            mockMvc.perform(put("/api/payments/{id}/fail", testPayment.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 결제 실패 처리")
        void failPayment_NotFound() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.FailPaymentRequest();
            request.setFailureReason("Test failure");

            // When & Then
            mockMvc.perform(put("/api/payments/{id}/fail", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("결제 환불 API 테스트")
    class RefundPaymentTest {

        private Payment testPayment;

        @BeforeEach
        void setUp() {
            testPayment = Payment.builder()
                    .order(testOrder)
                    .amount(testOrder.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            testPayment.process("TXN123456789");
            testPayment = paymentRepository.save(testPayment);
        }

        @Test
        @DisplayName("결제 환불 성공")
        void refundPayment_Success() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/payments/{id}/refund", testPayment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REFUNDED"));
        }

        @Test
        @DisplayName("완료되지 않은 결제 환불 실패")
        void refundPayment_NotCompleted_Failure() throws Exception {
            // Given - Create a separate order for the pending payment
            Order pendingOrder = Order.builder()
                    .orderNumber("ORD-PENDING")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(150.00))
                    .build();
            pendingOrder = orderRepository.save(pendingOrder);

            Payment pendingPayment = Payment.builder()
                    .order(pendingOrder)
                    .amount(pendingOrder.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.DEBIT_CARD)
                    .build();
            pendingPayment = paymentRepository.save(pendingPayment);

            // When & Then
            mockMvc.perform(put("/api/payments/{id}/refund", pendingPayment.getId()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 결제 환불 실패")
        void refundPayment_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/payments/{id}/refund", 999L))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("상태별 결제 조회 API 테스트")
    class GetPaymentsByStatusTest {

        @BeforeEach
        void setUp() {
            // 다양한 상태의 결제를 위한 별도 주문들 생성
            Order order2 = Order.builder()
                    .orderNumber("ORD-002")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            order2 = orderRepository.save(order2);

            Order order3 = Order.builder()
                    .orderNumber("ORD-003")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(300.00))
                    .build();
            order3 = orderRepository.save(order3);

            // 다양한 상태의 결제 생성
            Payment pendingPayment = Payment.builder()
                    .order(testOrder)
                    .amount(testOrder.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            paymentRepository.save(pendingPayment);

            Payment completedPayment = Payment.builder()
                    .order(order2)
                    .amount(order2.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.DEBIT_CARD)
                    .build();
            completedPayment.process("TXN123456789");
            paymentRepository.save(completedPayment);

            Payment failedPayment = Payment.builder()
                    .order(order3)
                    .amount(order3.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                    .build();
            failedPayment.fail("Insufficient funds");
            paymentRepository.save(failedPayment);
        }

        @Test
        @DisplayName("대기 상태 결제 조회")
        void getPaymentsByStatus_Pending() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/status/PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("완료된 결제 조회")
        void getPaymentsByStatus_Completed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/status/COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("COMPLETED"));
        }

        @Test
        @DisplayName("실패한 결제 조회")
        void getPaymentsByStatus_Failed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/status/FAILED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("FAILED"));
        }

        @Test
        @DisplayName("환불된 결제 조회")
        void getPaymentsByStatus_Refunded() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/status/REFUNDED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("회원별 결제 조회 API 테스트")
    class GetPaymentsByMemberTest {

        @BeforeEach
        void setUp() {
            // 다른 회원과 주문 생성
            Member member2 = Member.builder()
                    .email("test2@example.com")
                    .name("Test User 2")
                    .phoneNumber("010-2222-2222")
                    .build();
            member2 = memberRepository.save(member2);

            Order order2 = Order.builder()
                    .orderNumber("ORD-002")
                    .member(member2)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            order2 = orderRepository.save(order2);

            Payment payment2 = Payment.builder()
                    .order(order2)
                    .amount(order2.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            paymentRepository.save(payment2);
        }

        @Test
        @DisplayName("특정 회원의 모든 결제 조회")
        void getPaymentsByMemberId_Success() throws Exception {
            // Given
            Payment testPayment = Payment.builder()
                    .order(testOrder)
                    .amount(testOrder.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            paymentRepository.save(testPayment);

            // When & Then
            mockMvc.perform(get("/api/payments/member/{memberId}", testMember.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("존재하지 않는 회원의 결제 조회")
        void getPaymentsByMemberId_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/payments/member/{memberId}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("결제 검증 테스트")
    class PaymentValidationTest {

        @Test
        @DisplayName("잘못된 JSON 형식으로 결제 생성 실패")
        void createPayment_InvalidJson_Failure() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Content-Type이 없는 결제 생성 실패")
        void createPayment_NoContentType_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.PaymentController.CreatePaymentRequest();
            request.setOrderId(testOrder.getId());
            request.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("잘못된 결제 방법으로 결제 생성 실패")
        void createPayment_InvalidPaymentMethod_Failure() throws Exception {
            // Given
            String invalidRequest = """
                {
                    "orderId": %d,
                    "paymentMethod": "INVALID_METHOD"
                }
                """.formatted(testOrder.getId());

            // When & Then
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }
}