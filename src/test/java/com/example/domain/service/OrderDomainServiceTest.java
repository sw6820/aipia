package com.example.domain.service;

import com.example.domain.*;
import com.example.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderDomainService Tests")
class OrderDomainServiceTest {

    private OrderDomainService orderDomainService;
    private Member testMember;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderDomainService = new OrderDomainService();
        
        // Create test member
        testMember = Member.builder()
                .email("test@example.com")
                .name("Test Member")
                .phoneNumber("010-1234-5678")
                .build();
        
        // Create test order
        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(150.00))
                .build();
        
        // Create test order items
        OrderItem testOrderItem1 = OrderItem.builder()
                .productName("Product 1")
                .productDescription("Description 1")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build();
        
        OrderItem testOrderItem2 = OrderItem.builder()
                .productName("Product 2")
                .productDescription("Description 2")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build();
        
        // Add items to order
        testOrder.addOrderItem(testOrderItem1);
        testOrder.addOrderItem(testOrderItem2);
    }

    @Test
    @DisplayName("정상적인 주문 총액 계산")
    void calculateOrderTotal_Success() {
        // When
        Money total = orderDomainService.calculateOrderTotal(testOrder);
        
        // Then
        assertThat(total.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(total.getCurrency()).isEqualTo("KRW");
    }

    @Test
    @DisplayName("주문 항목이 없는 경우 예외 발생")
    void calculateOrderTotal_EmptyItems_ThrowsException() {
        // Given
        Order emptyOrder = Order.builder()
                .orderNumber("ORD-002")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(0.00))
                .build();
        
        // When & Then
        assertThatThrownBy(() -> orderDomainService.calculateOrderTotal(emptyOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must have at least one item");
    }

    @Test
    @DisplayName("올바른 총액 검증")
    void validateOrderTotal_ValidAmount_ReturnsTrue() {
        // When
        boolean isValid = orderDomainService.validateOrderTotal(testOrder);
        
        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 총액 검증")
    void validateOrderTotal_InvalidAmount_ReturnsFalse() {
        // Given - Create order with different total amount
        Order invalidOrder = Order.builder()
                .orderNumber("ORD-003")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(200.00)) // Different from calculated total
                .build();
        
        OrderItem item = OrderItem.builder()
                .productName("Product 3")
                .productDescription("Description 3")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build();
        
        invalidOrder.addOrderItem(item);
        
        // When
        boolean isValid = orderDomainService.validateOrderTotal(invalidOrder);
        
        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("취소 가능한 주문")
    void canCancelOrder_CancellableOrder_ReturnsTrue() {
        // When
        boolean canCancel = orderDomainService.canCancelOrder(testOrder);
        
        // Then
        assertThat(canCancel).isTrue();
    }

    @Test
    @DisplayName("완료된 주문은 취소 불가")
    void canCancelOrder_CompletedOrder_ReturnsFalse() {
        // Given
        testOrder.confirm(); // First confirm the order
        testOrder.complete(); // Then complete it
        
        // When
        boolean canCancel = orderDomainService.canCancelOrder(testOrder);
        
        // Then
        assertThat(canCancel).isFalse();
    }

    @Test
    @DisplayName("회원 총 구매액 계산")
    void calculateMemberTotalSpent_Success() {
        // Given
        Order order1 = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        order1.confirm();
        order1.complete();
        
        Order order2 = Order.builder()
                .orderNumber("ORD-002")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();
        order2.confirm();
        order2.complete();
        
        List<Order> orders = List.of(order1, order2);
        
        // When
        Money totalSpent = orderDomainService.calculateMemberTotalSpent(orders);
        
        // Then
        assertThat(totalSpent.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
    }

    @Test
    @DisplayName("주문이 없는 회원의 총 구매액은 0")
    void calculateMemberTotalSpent_NoOrders_ReturnsZero() {
        // When
        Money totalSpent = orderDomainService.calculateMemberTotalSpent(List.of());
        
        // Then
        assertThat(totalSpent.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
