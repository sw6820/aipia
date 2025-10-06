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

@DisplayName("Order 도메인 테스트")
class OrderTest {

    private Member testMember;
    private Order order;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();

        order = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
    }

    @Nested
    @DisplayName("주문 생성 테스트")
    class CreateOrderTest {

        @Test
        @DisplayName("정상적인 주문 생성 성공")
        void createOrder_Success() {
            // Given & When
            Order newOrder = Order.builder()
                    .orderNumber("ORD-002")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();

            // Then
            assertThat(newOrder.getOrderNumber()).isEqualTo("ORD-002");
            assertThat(newOrder.getMember()).isEqualTo(testMember);
            assertThat(newOrder.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200.00));
            assertThat(newOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        }

        @Test
        @DisplayName("주문번호가 null인 경우 예외 발생")
        void createOrder_NullOrderNumber_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Order.builder()
                    .orderNumber(null)
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("빈 주문번호로 주문 생성 시 예외 발생")
        void createOrder_EmptyOrderNumber_ThrowsException(String orderNumber) {
            // When & Then
            assertThatThrownBy(() -> Order.builder()
                    .orderNumber(orderNumber)
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("총액이 null인 경우 예외 발생")
        void createOrder_NullTotalAmount_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Order.builder()
                    .orderNumber("ORD-001")
                    .member(testMember)
                    .totalAmount(null)
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("총액이 음수인 경우 예외 발생")
        void createOrder_NegativeTotalAmount_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Order.builder()
                    .orderNumber("ORD-001")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(-100.00))
                    .build())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("주문 상태 변경 테스트")
    class OrderStatusTest {

        @Test
        @DisplayName("주문 확인 성공")
        void confirm_Success() {
            // Given
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);

            // When
            order.confirm();

            // Then
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("주문 취소 성공")
        void cancel_Success() {
            // Given
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);

            // When
            order.cancel();

            // Then
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("주문 완료 성공")
        void complete_Success() {
            // Given
            order.confirm();
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);

            // When
            order.complete();

            // Then
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("취소된 주문은 완료할 수 없음")
        void complete_CancelledOrder_ThrowsException() {
            // Given
            order.cancel();
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);

            // When & Then
            assertThatThrownBy(() -> order.complete())
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("완료된 주문은 취소할 수 없음")
        void cancel_CompletedOrder_ThrowsException() {
            // Given
            order.confirm();
            order.complete();
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);

            // When & Then
            assertThatThrownBy(() -> order.cancel())
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("주문 아이템 추가 테스트")
    class AddOrderItemTest {

        @Test
        @DisplayName("주문 아이템 추가 성공")
        void addOrderItem_Success() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .productName("Test Product")
                    .productDescription("Test Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(50.00))
                    .build();

            // When
            order.addOrderItem(orderItem);

            // Then
            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getOrderItems()).contains(orderItem);
            assertThat(orderItem.getOrder()).isEqualTo(order);
        }

        @Test
        @DisplayName("여러 주문 아이템 추가 성공")
        void addMultipleOrderItems_Success() {
            // Given
            OrderItem item1 = OrderItem.builder()
                    .productName("Product 1")
                    .productDescription("Description 1")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(30.00))
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .productName("Product 2")
                    .productDescription("Description 2")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(35.00))
                    .build();

            // When
            order.addOrderItem(item1);
            order.addOrderItem(item2);

            // Then
            assertThat(order.getOrderItems()).hasSize(2);
            assertThat(order.getOrderItems()).contains(item1, item2);
        }

        @Test
        @DisplayName("null 주문 아이템 추가 시 예외 발생")
        void addOrderItem_NullItem_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> order.addOrderItem(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("결제 설정 테스트")
    class SetPaymentTest {

        @Test
        @DisplayName("결제 설정 성공")
        void setPayment_Success() {
            // Given
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();

            // When
            order.setPayment(payment);

            // Then
            assertThat(order.getPayment()).isEqualTo(payment);
            assertThat(payment.getOrder()).isEqualTo(order);
        }

        @Test
        @DisplayName("null 결제 설정 시 예외 발생")
        void setPayment_NullPayment_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> order.setPayment(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("이미 결제가 설정된 주문에 새로운 결제 설정 시 기존 결제 교체")
        void setPayment_ReplaceExistingPayment_Success() {
            // Given
            Payment payment1 = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            order.setPayment(payment1);

            Payment payment2 = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.DEBIT_CARD)
                    .build();

            // When
            order.setPayment(payment2);

            // Then
            assertThat(order.getPayment()).isEqualTo(payment2);
            assertThat(payment2.getOrder()).isEqualTo(order);
        }
    }

    @Nested
    @DisplayName("주문 비즈니스 규칙 테스트")
    class OrderBusinessRulesTest {

        @Test
        @DisplayName("주문 생성 시 기본 상태는 PENDING")
        void createOrder_DefaultStatusIsPending() {
            // When
            Order newOrder = Order.builder()
                    .orderNumber("ORD-001")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();

            // Then
            assertThat(newOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        }

        @Test
        @DisplayName("주문 총액은 소수점 2자리까지 정확")
        void orderTotalAmount_PrecisionTest() {
            // Given
            BigDecimal preciseAmount = new BigDecimal("99.99");

            // When
            Order newOrder = Order.builder()
                    .orderNumber("ORD-001")
                    .member(testMember)
                    .totalAmount(preciseAmount)
                    .build();

            // Then
            assertThat(newOrder.getTotalAmount()).isEqualTo(preciseAmount);
            assertThat(newOrder.getTotalAmount().scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("주문 상태 변경 시 주문 아이템은 유지")
        void changeStatus_OrderItemsPreserved() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .productName("Test Product")
                    .productDescription("Test Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.00))
                    .build();
            order.addOrderItem(orderItem);

            // When
            order.confirm();

            // Then
            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("주문 상태 변경 시 결제 정보는 유지")
        void changeStatus_PaymentPreserved() {
            // Given
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                    .build();
            order.setPayment(payment);

            // When
            order.confirm();

            // Then
            assertThat(order.getPayment()).isEqualTo(payment);
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("주문 검증 테스트")
    class OrderValidationTest {

        @Test
        @DisplayName("주문번호 형식 검증")
        void orderNumberFormatValidation() {
            // Given
            String validOrderNumber = "ORD-20240101-001";

            // When
            Order newOrder = Order.builder()
                    .orderNumber(validOrderNumber)
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();

            // Then
            assertThat(newOrder.getOrderNumber()).isEqualTo(validOrderNumber);
        }

        @Test
        @DisplayName("총액 범위 검증")
        void totalAmountRangeValidation() {
            // Given
            BigDecimal maxAmount = new BigDecimal("999999999999999.99");

            // When
            Order newOrder = Order.builder()
                    .orderNumber("ORD-001")
                    .member(testMember)
                    .totalAmount(maxAmount)
                    .build();

            // Then
            assertThat(newOrder.getTotalAmount()).isEqualTo(maxAmount);
        }

        @Test
        @DisplayName("회원 정보 검증")
        void memberValidation() {
            // When
            Order newOrder = Order.builder()
                    .orderNumber("ORD-001")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();

            // Then
            assertThat(newOrder.getMember()).isEqualTo(testMember);
            assertThat(newOrder.getMember().getEmail()).isEqualTo("test@example.com");
        }
    }
}