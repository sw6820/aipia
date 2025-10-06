package com.example.dto;

import com.example.domain.Order;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long memberId;
    private String memberName;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDto> orderItems;
    private PaymentDto payment;

    // Setters for testing
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public void setOrderItems(List<OrderItemDto> orderItems) { this.orderItems = orderItems; }

    public static OrderDto from(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .memberId(order.getMember() != null ? order.getMember().getId() : null)
                .memberName(order.getMember() != null ? order.getMember().getName() : null)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::from)
                        .toList())
                .payment(order.getPayment() != null ? PaymentDto.from(order.getPayment()) : null)
                .build();
    }
}