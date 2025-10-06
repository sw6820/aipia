package com.example.domain.event;

import com.example.domain.Payment;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain events related to Payment entity.
 */
public class PaymentEvents {
    
    @Getter
    public static class PaymentCreated implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "PaymentCreated";
        
        private final Long paymentId;
        private final String transactionId;
        private final Long orderId;
        private final BigDecimal amount;
        private final String paymentMethod;
        
        public PaymentCreated(Payment payment) {
            this.paymentId = payment.getId();
            this.transactionId = payment.getTransactionId();
            this.orderId = payment.getOrder().getId();
            this.amount = payment.getAmount();
            this.paymentMethod = payment.getPaymentMethod().toString();
        }
        
        @Override
        public UUID getEventId() {
            return eventId;
        }
        
        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
    }
    
    @Getter
    public static class PaymentProcessed implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "PaymentProcessed";
        
        private final Long paymentId;
        private final String transactionId;
        private final Long orderId;
        private final BigDecimal amount;
        
        public PaymentProcessed(Payment payment) {
            this.paymentId = payment.getId();
            this.transactionId = payment.getTransactionId();
            this.orderId = payment.getOrder().getId();
            this.amount = payment.getAmount();
        }
        
        @Override
        public UUID getEventId() {
            return eventId;
        }
        
        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
    }
    
    @Getter
    public static class PaymentFailed implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "PaymentFailed";
        
        private final Long paymentId;
        private final String transactionId;
        private final Long orderId;
        private final String failureReason;
        
        public PaymentFailed(Payment payment) {
            this.paymentId = payment.getId();
            this.transactionId = payment.getTransactionId();
            this.orderId = payment.getOrder().getId();
            this.failureReason = payment.getFailureReason();
        }
        
        @Override
        public UUID getEventId() {
            return eventId;
        }
        
        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
    }
    
    @Getter
    public static class PaymentRefunded implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "PaymentRefunded";
        
        private final Long paymentId;
        private final String transactionId;
        private final Long orderId;
        private final BigDecimal refundAmount;
        
        public PaymentRefunded(Payment payment) {
            this.paymentId = payment.getId();
            this.transactionId = payment.getTransactionId();
            this.orderId = payment.getOrder().getId();
            this.refundAmount = payment.getAmount();
        }
        
        @Override
        public UUID getEventId() {
            return eventId;
        }
        
        @Override
        public LocalDateTime getOccurredOn() {
            return occurredOn;
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
    }
}