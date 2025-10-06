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

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 100)
    private String transactionId;

    @Column(length = 500)
    private String failureReason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Payment(Order order, BigDecimal amount, PaymentMethod paymentMethod) {
        if (order == null) {
            throw new NullPointerException("Order cannot be null");
        }
        if (amount == null) {
            throw new NullPointerException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (paymentMethod == null) {
            throw new NullPointerException("Payment method cannot be null");
        }
        this.order = order;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void process(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new NullPointerException("Transaction ID cannot be null or empty");
        }
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
    }

    public void fail(String failureReason) {
        if (failureReason == null || failureReason.trim().isEmpty()) {
            throw new NullPointerException("Failure reason cannot be null or empty");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    public void refund() {
        if (this.status == PaymentStatus.REFUNDED) {
            // Already refunded, no action needed
            return;
        }
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CASH
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}