package com.example.integration;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.infrastructure.persistence.PaymentRepository;
import com.example.application.service.MemberService;
import com.example.application.service.OrderService;
import com.example.application.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("완전한 워크플로우 통합 테스트")
class CompleteWorkflowIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Nested
    @DisplayName("회원 가입부터 결제 완료까지 전체 플로우")
    class CompleteOrderFlowTest {

        @Test
        @DisplayName("회원 가입 → 주문 생성 → 결제 처리 → 주문 완료 전체 플로우")
        void completeOrderFlow_Success() {
            // 1. 회원 가입
            var memberDto = memberService.createMember(
                    "newuser@example.com",
                    "New User",
                    "010-9999-8888"
            );
            assertThat(memberDto.getEmail()).isEqualTo("newuser@example.com");
            assertThat(memberDto.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);

            // 2. 주문 생성
            var orderItem1 = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem1.setProductName("Laptop");
            orderItem1.setProductDescription("High-performance laptop");
            orderItem1.setQuantity(1);
            orderItem1.setUnitPrice(BigDecimal.valueOf(1000.00));

            var orderItem2 = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem2.setProductName("Mouse");
            orderItem2.setProductDescription("Wireless mouse");
            orderItem2.setQuantity(2);
            orderItem2.setUnitPrice(BigDecimal.valueOf(25.00));

            var orderDto = orderService.createOrder(
                    memberDto.getId(),
                    List.of(orderItem1, orderItem2)
            );
            assertThat(orderDto.getOrderNumber()).isNotEmpty();
            assertThat(orderDto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1050.00));
            assertThat(orderDto.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
            assertThat(orderDto.getOrderItems()).hasSize(2);

            // 3. 주문 확인
            var confirmedOrderDto = orderService.confirmOrder(orderDto.getId());
            assertThat(confirmedOrderDto.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);

            // 4. 결제 생성
            var paymentDto = paymentService.createPayment(
                    orderDto.getId(),
                    Payment.PaymentMethod.CREDIT_CARD
            );
            assertThat(paymentDto.getOrderId()).isEqualTo(orderDto.getId());
            assertThat(paymentDto.getAmount()).isEqualTo(BigDecimal.valueOf(1050.00));
            assertThat(paymentDto.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
            assertThat(paymentDto.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);

            // 5. 결제 처리
            var processedPaymentDto = paymentService.processPayment(paymentDto.getId());
            assertThat(processedPaymentDto.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
            assertThat(processedPaymentDto.getTransactionId()).isNotEmpty();

            // 6. 주문 완료
            var completedOrderDto = orderService.completeOrder(orderDto.getId());
            assertThat(completedOrderDto.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);

            // 7. 최종 검증
            var finalMember = memberService.getMemberById(memberDto.getId()).orElseThrow();
            var finalOrder = orderService.getOrderById(orderDto.getId()).orElseThrow();
            var finalPayment = paymentService.getPaymentById(paymentDto.getId()).orElseThrow();

            assertThat(finalMember.getOrders()).hasSize(1);
            assertThat(finalOrder.getPayment()).isNotNull();
            assertThat(finalPayment.getOrderId()).isEqualTo(finalOrder.getId());
            assertThat(finalOrder.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
            assertThat(finalPayment.getStatus()).isEqualTo(Payment.PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("결제 실패 시 주문 취소 플로우")
        void paymentFailureAndOrderCancellationFlow_Success() {
            // 1. 회원 가입
            var memberDto = memberService.createMember(
                    "failureuser@example.com",
                    "Failure User",
                    "010-7777-6666"
            );

            // 2. 주문 생성
            var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem.setProductName("Expensive Item");
            orderItem.setProductDescription("Very expensive item");
            orderItem.setQuantity(1);
            orderItem.setUnitPrice(BigDecimal.valueOf(10000.00));

            var orderDto = orderService.createOrder(
                    memberDto.getId(),
                    List.of(orderItem)
            );

            // 3. 주문 확인
            orderService.confirmOrder(orderDto.getId());

            // 4. 결제 생성
            var paymentDto = paymentService.createPayment(
                    orderDto.getId(),
                    Payment.PaymentMethod.CREDIT_CARD
            );

            // 5. 결제 실패
            var failedPaymentDto = paymentService.failPayment(
                    paymentDto.getId(),
                    "Insufficient credit limit"
            );
            assertThat(failedPaymentDto.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
            assertThat(failedPaymentDto.getFailureReason()).isEqualTo("Insufficient credit limit");

            // 6. 주문 취소
            var cancelledOrderDto = orderService.cancelOrder(orderDto.getId());
            assertThat(cancelledOrderDto.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);

            // 7. 최종 검증
            var finalOrder = orderService.getOrderById(orderDto.getId()).orElseThrow();
            var finalPayment = paymentService.getPaymentById(paymentDto.getId()).orElseThrow();

            assertThat(finalOrder.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
            assertThat(finalPayment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("결제 환불 플로우")
        void paymentRefundFlow_Success() {
            // 1. 회원 가입
            var memberDto = memberService.createMember(
                    "refunduser@example.com",
                    "Refund User",
                    "010-5555-4444"
            );

            // 2. 주문 생성 및 확인
            var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem.setProductName("Refundable Item");
            orderItem.setProductDescription("Item that can be refunded");
            orderItem.setQuantity(1);
            orderItem.setUnitPrice(BigDecimal.valueOf(500.00));

            var orderDto = orderService.createOrder(
                    memberDto.getId(),
                    List.of(orderItem)
            );
            orderService.confirmOrder(orderDto.getId());

            // 3. 결제 생성 및 처리
            var paymentDto = paymentService.createPayment(
                    orderDto.getId(),
                    Payment.PaymentMethod.CREDIT_CARD
            );
            paymentService.processPayment(paymentDto.getId());

            // 4. 주문 완료
            orderService.completeOrder(orderDto.getId());

            // 5. 결제 환불
            var refundedPaymentDto = paymentService.refundPayment(paymentDto.getId());
            assertThat(refundedPaymentDto.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);

            // 6. 최종 검증
            var finalPayment = paymentService.getPaymentById(paymentDto.getId()).orElseThrow();
            assertThat(finalPayment.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);
        }
    }

    @Nested
    @DisplayName("복잡한 주문 시나리오")
    class ComplexOrderScenariosTest {

        @Test
        @DisplayName("한 회원의 여러 주문 처리")
        void multipleOrdersForSingleMember_Success() {
            // 1. 회원 가입
            var memberDto = memberService.createMember(
                    "multiuser@example.com",
                    "Multi User",
                    "010-3333-2222"
            );

            // 2. 첫 번째 주문
            var orderItem1 = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem1.setProductName("Book");
            orderItem1.setProductDescription("Programming book");
            orderItem1.setQuantity(2);
            orderItem1.setUnitPrice(BigDecimal.valueOf(30.00));

            var order1Dto = orderService.createOrder(
                    memberDto.getId(),
                    List.of(orderItem1)
            );

            // 3. 두 번째 주문
            var orderItem2 = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem2.setProductName("Coffee");
            orderItem2.setProductDescription("Premium coffee");
            orderItem2.setQuantity(1);
            orderItem2.setUnitPrice(BigDecimal.valueOf(15.00));

            var order2Dto = orderService.createOrder(
                    memberDto.getId(),
                    List.of(orderItem2)
            );

            // 4. 각 주문에 대한 결제 처리
            var payment1Dto = paymentService.createPayment(
                    order1Dto.getId(),
                    Payment.PaymentMethod.CREDIT_CARD
            );
            paymentService.processPayment(payment1Dto.getId());

            var payment2Dto = paymentService.createPayment(
                    order2Dto.getId(),
                    Payment.PaymentMethod.DEBIT_CARD
            );
            paymentService.processPayment(payment2Dto.getId());

            // 5. 주문 확인 및 완료
            orderService.confirmOrder(order1Dto.getId());
            orderService.confirmOrder(order2Dto.getId());
            orderService.completeOrder(order1Dto.getId());
            orderService.completeOrder(order2Dto.getId());

            // 6. 검증
            var memberOrders = orderService.getOrdersByMemberId(memberDto.getId());
            assertThat(memberOrders).hasSize(2);

            var memberPayments = paymentService.getPaymentsByMemberId(memberDto.getId());
            assertThat(memberPayments).hasSize(2);

            var finalMember = memberService.getMemberById(memberDto.getId()).orElseThrow();
            assertThat(finalMember.getOrders()).hasSize(2);
        }

        @Test
        @DisplayName("대량 주문 아이템 처리")
        void largeOrderWithManyItems_Success() {
            // 1. 회원 가입
            var memberDto = memberService.createMember(
                    "bulkuser@example.com",
                    "Bulk User",
                    "010-1111-0000"
            );

            // 2. 대량 주문 아이템 생성
            var orderItems = List.of(
                    createOrderItem("Item 1", "Description 1", 5, BigDecimal.valueOf(10.00)),
                    createOrderItem("Item 2", "Description 2", 3, BigDecimal.valueOf(20.00)),
                    createOrderItem("Item 3", "Description 3", 2, BigDecimal.valueOf(50.00)),
                    createOrderItem("Item 4", "Description 4", 1, BigDecimal.valueOf(100.00)),
                    createOrderItem("Item 5", "Description 5", 4, BigDecimal.valueOf(25.00))
            );

            // 3. 주문 생성
            var orderDto = orderService.createOrder(memberDto.getId(), orderItems);
            assertThat(orderDto.getOrderItems()).hasSize(5);
            assertThat(orderDto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(410.00)); // 50+60+100+100+100

            // 4. 주문 확인 및 결제 처리
            orderService.confirmOrder(orderDto.getId());
            var paymentDto = paymentService.createPayment(
                    orderDto.getId(),
                    Payment.PaymentMethod.BANK_TRANSFER
            );
            paymentService.processPayment(paymentDto.getId());
            orderService.completeOrder(orderDto.getId());

            // 5. 검증
            var finalOrder = orderService.getOrderById(orderDto.getId()).orElseThrow();
            assertThat(finalOrder.getOrderItems()).hasSize(5);
            assertThat(finalOrder.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
        }

        private com.example.application.service.OrderService.OrderItemRequest createOrderItem(
                String productName, String description, Integer quantity, BigDecimal unitPrice) {
            var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem.setProductName(productName);
            orderItem.setProductDescription(description);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            return orderItem;
        }
    }

    @Nested
    @DisplayName("에러 시나리오 및 예외 처리")
    class ErrorScenariosTest {

        @Test
        @DisplayName("존재하지 않는 회원으로 주문 생성 실패")
        void createOrderWithNonExistentMember_Failure() {
            // When & Then
            assertThatThrownBy(() -> {
                var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
                orderItem.setProductName("Test Product");
                orderItem.setProductDescription("Test Description");
                orderItem.setQuantity(1);
                orderItem.setUnitPrice(BigDecimal.valueOf(100.00));

                orderService.createOrder(999L, List.of(orderItem));
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문으로 결제 생성 실패")
        void createPaymentForNonExistentOrder_Failure() {
            // When & Then
            assertThatThrownBy(() -> {
                paymentService.createPayment(999L, Payment.PaymentMethod.CREDIT_CARD);
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("완료되지 않은 결제 환불 실패")
        void refundPendingPayment_Failure() {
            // 1. 회원 및 주문 생성
            var memberDto = memberService.createMember(
                    "erroruser@example.com",
                    "Error User",
                    "010-9999-0000"
            );

            var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem.setProductName("Test Product");
            orderItem.setProductDescription("Test Description");
            orderItem.setQuantity(1);
            orderItem.setUnitPrice(BigDecimal.valueOf(100.00));

            var orderDto = orderService.createOrder(
                    memberDto.getId(),
                    List.of(orderItem)
            );

            // 2. 결제 생성 (처리하지 않음)
            var paymentDto = paymentService.createPayment(
                    orderDto.getId(),
                    Payment.PaymentMethod.CREDIT_CARD
            );

            // 3. 환불 시도 (실패해야 함)
            assertThatThrownBy(() -> {
                paymentService.refundPayment(paymentDto.getId());
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("취소된 주문 완료 실패")
        void completeCancelledOrder_Failure() {
            // 1. 회원 및 주문 생성
            var memberDto = memberService.createMember(
                    "canceluser@example.com",
                    "Cancel User",
                    "010-8888-7777"
            );

            var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem.setProductName("Test Product");
            orderItem.setProductDescription("Test Description");
            orderItem.setQuantity(1);
            orderItem.setUnitPrice(BigDecimal.valueOf(100.00));

            var orderDto = orderService.createOrder(
                    memberDto.getId(),
                    List.of(orderItem)
            );

            // 2. 주문 취소
            orderService.cancelOrder(orderDto.getId());

            // 3. 주문 완료 시도 (실패해야 함)
            assertThatThrownBy(() -> {
                orderService.completeOrder(orderDto.getId());
            }).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("데이터 일관성 검증")
    class DataConsistencyTest {

        @Test
        @DisplayName("주문과 결제 간 데이터 일관성 검증")
        void orderPaymentDataConsistency_Success() {
            // 1. 회원 가입
            var memberDto = memberService.createMember(
                    "consistency@example.com",
                    "Consistency User",
                    "010-6666-5555"
            );

            // 2. 주문 생성
            var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
            orderItem.setProductName("Consistency Test Item");
            orderItem.setProductDescription("Testing data consistency");
            orderItem.setQuantity(2);
            orderItem.setUnitPrice(BigDecimal.valueOf(75.00));

            var orderDto = orderService.createOrder(
                    memberDto.getId(),
                    List.of(orderItem)
            );

            // 3. 결제 생성
            var paymentDto = paymentService.createPayment(
                    orderDto.getId(),
                    Payment.PaymentMethod.CREDIT_CARD
            );

            // 4. 데이터 일관성 검증
            assertThat(paymentDto.getOrderId()).isEqualTo(orderDto.getId());
            assertThat(paymentDto.getAmount()).isEqualTo(orderDto.getTotalAmount());

            // 5. 결제 처리 후 재검증
            paymentService.processPayment(paymentDto.getId());
            orderService.confirmOrder(orderDto.getId());
            orderService.completeOrder(orderDto.getId());

            var finalOrder = orderService.getOrderById(orderDto.getId()).orElseThrow();
            var finalPayment = paymentService.getPaymentById(paymentDto.getId()).orElseThrow();

            assertThat(finalOrder.getPayment()).isNotNull();
            assertThat(finalPayment.getOrderId()).isEqualTo(finalOrder.getId());
            assertThat(finalOrder.getPayment().getId()).isEqualTo(finalPayment.getId());
            assertThat(finalPayment.getOrderId()).isEqualTo(finalOrder.getId());
        }

        @Test
        @DisplayName("회원과 주문 간 데이터 일관성 검증")
        void memberOrderDataConsistency_Success() {
            // 1. 회원 가입
            var memberDto = memberService.createMember(
                    "memberconsistency@example.com",
                    "Member Consistency User",
                    "010-4444-3333"
            );

            // 2. 여러 주문 생성
            for (int i = 0; i < 3; i++) {
                var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
                orderItem.setProductName("Item " + (i + 1));
                orderItem.setProductDescription("Description " + (i + 1));
                orderItem.setQuantity(1);
                orderItem.setUnitPrice(BigDecimal.valueOf(100.00 + i * 10));

                orderService.createOrder(memberDto.getId(), List.of(orderItem));
            }

            // 3. 데이터 일관성 검증
            var memberOrders = orderService.getOrdersByMemberId(memberDto.getId());
            assertThat(memberOrders).hasSize(3);

            var finalMember = memberService.getMemberById(memberDto.getId()).orElseThrow();
            assertThat(finalMember.getOrders()).hasSize(3);

            // 모든 주문이 올바른 회원을 참조하는지 확인
            for (var orderDto : memberOrders) {
                assertThat(orderDto.getMemberId()).isEqualTo(memberDto.getId());
                assertThat(orderDto.getMemberName()).isEqualTo(memberDto.getName());
            }
        }
    }
}