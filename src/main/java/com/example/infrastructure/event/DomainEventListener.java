package com.example.infrastructure.event;

import com.example.domain.event.MemberEvents;
import com.example.domain.event.OrderEvents;
import com.example.domain.event.PaymentEvents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for domain events.
 * Handles domain events and performs side effects like logging, notifications, etc.
 */
@Component
@Slf4j
public class DomainEventListener {
    
    // Member Events
    @EventListener
    public void handleMemberCreated(MemberEvents.MemberCreated event) {
        log.info("Member created: ID={}, Email={}, Name={}", 
                event.getMemberId(), event.getEmail(), event.getName());
        // Here you could send welcome email, create audit log, etc.
    }
    
    @EventListener
    public void handleMemberDeactivated(MemberEvents.MemberDeactivated event) {
        log.info("Member deactivated: ID={}, Email={}", 
                event.getMemberId(), event.getEmail());
        // Here you could send notification, update external systems, etc.
    }
    
    @EventListener
    public void handleMemberActivated(MemberEvents.MemberActivated event) {
        log.info("Member activated: ID={}, Email={}", 
                event.getMemberId(), event.getEmail());
        // Here you could send reactivation email, update external systems, etc.
    }
    
    // Order Events
    @EventListener
    public void handleOrderCreated(OrderEvents.OrderCreated event) {
        log.info("Order created: ID={}, OrderNumber={}, MemberID={}, Amount={}", 
                event.getOrderId(), event.getOrderNumber(), event.getMemberId(), event.getTotalAmount());
        // Here you could send order confirmation email, update inventory, etc.
    }
    
    @EventListener
    public void handleOrderConfirmed(OrderEvents.OrderConfirmed event) {
        log.info("Order confirmed: ID={}, OrderNumber={}, MemberID={}", 
                event.getOrderId(), event.getOrderNumber(), event.getMemberId());
        // Here you could trigger payment processing, update inventory, etc.
    }
    
    @EventListener
    public void handleOrderCancelled(OrderEvents.OrderCancelled event) {
        log.info("Order cancelled: ID={}, OrderNumber={}, MemberID={}, Reason={}", 
                event.getOrderId(), event.getOrderNumber(), event.getMemberId(), event.getReason());
        // Here you could send cancellation email, restore inventory, etc.
    }
    
    @EventListener
    public void handleOrderCompleted(OrderEvents.OrderCompleted event) {
        log.info("Order completed: ID={}, OrderNumber={}, MemberID={}, Amount={}", 
                event.getOrderId(), event.getOrderNumber(), event.getMemberId(), event.getTotalAmount());
        // Here you could send completion email, update member statistics, etc.
    }
    
    // Payment Events
    @EventListener
    public void handlePaymentCreated(PaymentEvents.PaymentCreated event) {
        log.info("Payment created: ID={}, TransactionID={}, OrderID={}, Amount={}, Method={}", 
                event.getPaymentId(), event.getTransactionId(), event.getOrderId(), 
                event.getAmount(), event.getPaymentMethod());
        // Here you could log payment initiation, send notification, etc.
    }
    
    @EventListener
    public void handlePaymentProcessed(PaymentEvents.PaymentProcessed event) {
        log.info("Payment processed: ID={}, TransactionID={}, OrderID={}, Amount={}", 
                event.getPaymentId(), event.getTransactionId(), event.getOrderId(), event.getAmount());
        // Here you could update order status, send confirmation, etc.
    }
    
    @EventListener
    public void handlePaymentFailed(PaymentEvents.PaymentFailed event) {
        log.warn("Payment failed: ID={}, TransactionID={}, OrderID={}, Reason={}", 
                event.getPaymentId(), event.getTransactionId(), event.getOrderId(), event.getFailureReason());
        // Here you could send failure notification, update order status, etc.
    }
    
    @EventListener
    public void handlePaymentRefunded(PaymentEvents.PaymentRefunded event) {
        log.info("Payment refunded: ID={}, TransactionID={}, OrderID={}, Amount={}", 
                event.getPaymentId(), event.getTransactionId(), event.getOrderId(), event.getRefundAmount());
        // Here you could send refund confirmation, update order status, etc.
    }
}