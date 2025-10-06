package com.example.domain.event;

import com.example.domain.Order;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain events related to Order entity.
 */
public class OrderEvents {
    
    @Getter
    public static class OrderCreated implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "OrderCreated";
        
        private final Long orderId;
        private final String orderNumber;
        private final Long memberId;
        private final BigDecimal totalAmount;
        
        public OrderCreated(Order order) {
            this.orderId = order.getId();
            this.orderNumber = order.getOrderNumber();
            this.memberId = order.getMember().getId();
            this.totalAmount = order.getTotalAmount();
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
    public static class OrderConfirmed implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "OrderConfirmed";
        
        private final Long orderId;
        private final String orderNumber;
        private final Long memberId;
        
        public OrderConfirmed(Order order) {
            this.orderId = order.getId();
            this.orderNumber = order.getOrderNumber();
            this.memberId = order.getMember().getId();
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
    public static class OrderCancelled implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "OrderCancelled";
        
        private final Long orderId;
        private final String orderNumber;
        private final Long memberId;
        private final String reason;
        
        public OrderCancelled(Order order, String reason) {
            this.orderId = order.getId();
            this.orderNumber = order.getOrderNumber();
            this.memberId = order.getMember().getId();
            this.reason = reason;
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
    public static class OrderCompleted implements DomainEvent {
        private final UUID eventId = UUID.randomUUID();
        private final LocalDateTime occurredOn = LocalDateTime.now();
        private final String eventType = "OrderCompleted";
        
        private final Long orderId;
        private final String orderNumber;
        private final Long memberId;
        private final BigDecimal totalAmount;
        
        public OrderCompleted(Order order) {
            this.orderId = order.getId();
            this.orderNumber = order.getOrderNumber();
            this.memberId = order.getMember().getId();
            this.totalAmount = order.getTotalAmount();
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