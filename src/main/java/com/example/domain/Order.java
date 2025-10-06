package com.example.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Order(String orderNumber, Member member, BigDecimal totalAmount) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new NullPointerException("Order number cannot be null or empty");
        }
        if (member == null) {
            throw new NullPointerException("Member cannot be null");
        }
        if (totalAmount == null) {
            throw new NullPointerException("Total amount cannot be null");
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative");
        }
        this.orderNumber = orderNumber;
        this.member = member;
        this.totalAmount = totalAmount;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new NullPointerException("Order item cannot be null");
        }
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setPayment(Payment payment) {
        if (payment == null) {
            throw new NullPointerException("Payment cannot be null");
        }
        this.payment = payment;
    }

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void complete() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled orders cannot be completed");
        }
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be completed");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }
}