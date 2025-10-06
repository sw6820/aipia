package com.example.service;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.application.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private OrderService orderService;

    private Member testMember;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testMember = new Member() {
            @Override
            public Long getId() { return 1L; }
            @Override
            public String getEmail() { return "test@example.com"; }
            @Override
            public String getName() { return "Test User"; }
            @Override
            public String getPhoneNumber() { return "010-1234-5678"; }
            @Override
            public Member.MemberStatus getStatus() { return Member.MemberStatus.ACTIVE; }
        };

        testOrder = new Order() {
            @Override
            public Long getId() { return 1L; }
            @Override
            public String getOrderNumber() { return "ORD-20240101120000"; }
            @Override
            public Member getMember() { return testMember; }
            @Override
            public BigDecimal getTotalAmount() { return new BigDecimal("100.00"); }
            @Override
            public Order.OrderStatus getStatus() { return Order.OrderStatus.PENDING; }
        };
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        var orderItems = List.of(
            new com.example.application.service.OrderService.OrderItemRequest("Product 1", "Description 1", 2, new BigDecimal("50.00"))
        );

        // When
        var result = orderService.createOrder(1L, orderItems);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD-20240101120000");
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("100.00"));
        
        verify(memberRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 주문 생성 실패")
    void createOrder_MemberNotFound_Failure() {
        // Given
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        var orderItems = List.of(
            new com.example.application.service.OrderService.OrderItemRequest("Product 1", "Description 1", 2, new BigDecimal("50.00"))
        );

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(999L, orderItems))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
        
        verify(memberRepository).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("ID로 주문 조회 성공")
    void getOrderById_Success() {
        // Given
        when(orderRepository.findByIdWithOrderItemsAndPayment(1L)).thenReturn(Optional.of(testOrder));

        // When
        var result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo("ORD-20240101120000");
        
        verify(orderRepository).findByIdWithOrderItemsAndPayment(1L);
    }

    @Test
    @DisplayName("주문번호로 주문 조회 성공")
    void getOrderByOrderNumber_Success() {
        // Given
        when(orderRepository.findByOrderNumber("ORD-20240101120000")).thenReturn(Optional.of(testOrder));

        // When
        var result = orderService.getOrderByOrderNumber("ORD-20240101120000");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo("ORD-20240101120000");
        
        verify(orderRepository).findByOrderNumber("ORD-20240101120000");
    }

    @Test
    @DisplayName("주문 확인 성공")
    void confirmOrder_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        var result = orderService.confirmOrder(1L);

        // Then
        assertThat(result).isNotNull();
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        var result = orderService.cancelOrder(1L);

        // Then
        assertThat(result).isNotNull();
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 완료 성공")
    void completeOrder_Success() {
        // Given - Create a real confirmed order
        Order confirmedOrder = Order.builder()
                .orderNumber("ORD-20240101120000")
                .member(testMember)
                .totalAmount(new BigDecimal("100.00"))
                .build();
        confirmedOrder.confirm(); // Set status to CONFIRMED
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(confirmedOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);

        // When
        var result = orderService.completeOrder(1L);

        // Then
        assertThat(result).isNotNull();
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }
}