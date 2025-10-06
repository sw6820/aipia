package com.example.dto;

import com.example.domain.OrderItem;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class OrderItemDto {
    private Long id;
    private String productName;
    private String productDescription;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Setters for testing
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public static OrderItemDto from(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .productName(orderItem.getProductName())
                .productDescription(orderItem.getProductDescription())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}