package com.example.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false, length = 500)
    private String productDescription;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Builder
    public OrderItem(String productName, String productDescription, Integer quantity, BigDecimal unitPrice) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (productDescription == null || productDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("상품 설명은 필수입니다");
        }
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("단가는 0 이상이어야 합니다");
        }
        
        this.productName = productName;
        this.productDescription = productDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void setOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("주문은 필수입니다");
        }
        this.order = order;
    }

    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다");
        }
        this.quantity = newQuantity;
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(newQuantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(productName, orderItem.productName) &&
               Objects.equals(productDescription, orderItem.productDescription) &&
               Objects.equals(quantity, orderItem.quantity) &&
               Objects.equals(unitPrice, orderItem.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, productDescription, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
               "id=" + id +
               ", productName='" + productName + '\'' +
               ", productDescription='" + productDescription + '\'' +
               ", quantity=" + quantity +
               ", unitPrice=" + unitPrice +
               ", totalPrice=" + totalPrice +
               '}';
    }
}