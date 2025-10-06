package com.example.integration;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Member-Order-Payment 통합 테스트")
class MemberOrderPaymentIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("integration@example.com")
                .name("Integration Test User")
                .phoneNumber("010-9999-8888")
                .build();
        testMember = entityManager.persistAndFlush(testMember);
    }

    @Test
    @DisplayName("회원-주문-결제 전체 플로우 테스트")
    void memberOrderPaymentFlow_Success() {
        // Given: 회원이 주문을 생성
        Order order = Order.builder()
                .orderNumber("ORD-INTEGRATION-001")
                .member(testMember)
                .totalAmount(new BigDecimal("150.00"))
                .build();
        order = entityManager.persistAndFlush(order);
        
        // Manually add the order to the member since we're not using the service layer
        testMember.addOrder(order);
        entityManager.persistAndFlush(testMember);

        // When: 주문에 결제를 추가
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .build();
        payment = entityManager.persistAndFlush(payment);
        
        // Manually set the bidirectional relationship
        order.setPayment(payment);
        entityManager.persistAndFlush(order);

        // Then: 모든 관계가 올바르게 설정되었는지 확인
        Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertThat(foundMember.getOrders()).hasSize(1);
        assertThat(foundMember.getOrders().get(0).getOrderNumber()).isEqualTo("ORD-INTEGRATION-001");

        Order foundOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(foundOrder.getMember().getId()).isEqualTo(testMember.getId());
        assertThat(foundOrder.getPayment()).isNotNull();
        assertThat(foundOrder.getPayment().getAmount()).isEqualTo(new BigDecimal("150.00"));

        Payment foundPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(foundPayment.getOrder().getId()).isEqualTo(order.getId());
        assertThat(foundPayment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
    }

    @Test
    @DisplayName("회원의 모든 주문 조회 테스트")
    void getOrdersByMember_Success() {
        // Given: 여러 주문 생성
        Order order1 = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(new BigDecimal("100.00"))
                .build();
        entityManager.persistAndFlush(order1);

        Order order2 = Order.builder()
                .orderNumber("ORD-002")
                .member(testMember)
                .totalAmount(new BigDecimal("200.00"))
                .build();
        entityManager.persistAndFlush(order2);

        // When: 회원의 주문 조회
        List<Order> orders = orderRepository.findByMemberId(testMember.getId());

        // Then: 모든 주문이 조회되는지 확인
        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getOrderNumber)
                .containsExactlyInAnyOrder("ORD-001", "ORD-002");
    }

    @Test
    @DisplayName("주문 상태 변경 테스트")
    void orderStatusChange_Success() {
        // Given: 주문 생성
        Order order = Order.builder()
                .orderNumber("ORD-STATUS-001")
                .member(testMember)
                .totalAmount(new BigDecimal("100.00"))
                .build();
        order = entityManager.persistAndFlush(order);

        // When: 주문 상태 변경
        order.confirm();
        entityManager.persistAndFlush(order);

        order.complete();
        entityManager.persistAndFlush(order);

        // Then: 상태 변경 확인
        Order foundOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(foundOrder.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("결제 상태 변경 테스트")
    void paymentStatusChange_Success() {
        // Given: 주문과 결제 생성
        Order order = Order.builder()
                .orderNumber("ORD-PAYMENT-001")
                .member(testMember)
                .totalAmount(new BigDecimal("100.00"))
                .build();
        order = entityManager.persistAndFlush(order);

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .build();
        payment = entityManager.persistAndFlush(payment);

        // When: 결제 처리
        payment.process("TXN123456789");
        entityManager.persistAndFlush(payment);

        // Then: 결제 상태 확인
        Payment foundPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(foundPayment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        assertThat(foundPayment.getTransactionId()).isEqualTo("TXN123456789");
    }

    @Test
    @DisplayName("회원 비활성화 시 연관 데이터 확인")
    void memberDeactivation_Success() {
        // Given: 주문과 결제가 있는 회원
        Order order = Order.builder()
                .orderNumber("ORD-DEACTIVATE-001")
                .member(testMember)
                .totalAmount(new BigDecimal("100.00"))
                .build();
        entityManager.persistAndFlush(order);

        // When: 회원 비활성화
        testMember.deactivate();
        entityManager.persistAndFlush(testMember);

        // Then: 회원 상태 확인
        Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertThat(foundMember.getStatus()).isEqualTo(Member.MemberStatus.INACTIVE);
    }
}