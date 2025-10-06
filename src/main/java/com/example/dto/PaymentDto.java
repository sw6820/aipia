package com.example.dto;

import com.example.domain.Payment;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class PaymentDto {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Setters for testing
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setPaymentMethod(Payment.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public static PaymentDto from(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}