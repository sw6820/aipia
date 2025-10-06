package com.example.domain.service;

import com.example.domain.Order;
import com.example.domain.OrderItem;
import com.example.domain.Payment;
import com.example.domain.valueobject.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain service for complex order-related business logic.
 * Contains business rules that don't naturally belong to a single entity.
 */
@Service
@Slf4j
public class OrderDomainService {
    
    /**
     * Calculates the total amount for an order based on its items.
     * This is a business rule that involves multiple entities.
     */
    public Money calculateOrderTotal(Order order) {
        log.debug("Calculating total for order: {}", order.getOrderNumber());
        
        List<OrderItem> items = order.getOrderItems();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        BigDecimal total = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Money totalMoney = Money.krw(total);
        log.debug("Calculated total for order {}: {}", order.getOrderNumber(), totalMoney);
        
        return totalMoney;
    }
    
    /**
     * Validates that the order total matches the sum of its items.
     * This is a business rule for data consistency.
     */
    public boolean validateOrderTotal(Order order) {
        Money calculatedTotal = calculateOrderTotal(order);
        Money orderTotal = Money.krw(order.getTotalAmount());
        
        boolean isValid = calculatedTotal.equals(orderTotal);
        
        if (!isValid) {
            log.warn("Order total mismatch for order {}: expected {}, actual {}", 
                    order.getOrderNumber(), calculatedTotal, orderTotal);
        }
        
        return isValid;
    }
    
    /**
     * Determines if an order can be cancelled based on business rules.
     */
    public boolean canCancelOrder(Order order) {
        log.debug("Checking if order can be cancelled: {}", order.getOrderNumber());
        
        // Business rule: Only pending orders can be cancelled
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            log.debug("Order {} cannot be cancelled: status is {}", 
                     order.getOrderNumber(), order.getStatus());
            return false;
        }
        
        // Business rule: Orders with completed payments cannot be cancelled
        if (order.getPayment() != null && 
            order.getPayment().getStatus() == Payment.PaymentStatus.COMPLETED) {
            log.debug("Order {} cannot be cancelled: payment is completed", 
                     order.getOrderNumber());
            return false;
        }
        
        log.debug("Order {} can be cancelled", order.getOrderNumber());
        return true;
    }
    
    /**
     * Determines if an order can be completed based on business rules.
     */
    public boolean canCompleteOrder(Order order) {
        log.debug("Checking if order can be completed: {}", order.getOrderNumber());
        
        // Business rule: Only pending orders can be completed
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            log.debug("Order {} cannot be completed: status is {}", 
                     order.getOrderNumber(), order.getStatus());
            return false;
        }
        
        // Business rule: Order must have a completed payment
        if (order.getPayment() == null || 
            order.getPayment().getStatus() != Payment.PaymentStatus.COMPLETED) {
            log.debug("Order {} cannot be completed: payment not completed", 
                     order.getOrderNumber());
            return false;
        }
        
        // Business rule: Order total must match payment amount
        Money orderTotal = Money.krw(order.getTotalAmount());
        Money paymentAmount = Money.krw(order.getPayment().getAmount());
        
        if (!orderTotal.equals(paymentAmount)) {
            log.debug("Order {} cannot be completed: total mismatch", 
                     order.getOrderNumber());
            return false;
        }
        
        log.debug("Order {} can be completed", order.getOrderNumber());
        return true;
    }
    
    /**
     * Calculates the total amount for all orders of a member.
     */
    public Money calculateMemberTotalSpent(List<Order> orders) {
        log.debug("Calculating total spent for member with {} orders", orders.size());
        
        BigDecimal total = orders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Money totalMoney = Money.krw(total);
        log.debug("Calculated total spent: {}", totalMoney);
        
        return totalMoney;
    }
    
    /**
     * Determines if an order qualifies for a discount based on business rules.
     */
    public boolean qualifiesForDiscount(Order order, Money discountThreshold) {
        Money orderTotal = Money.krw(order.getTotalAmount());
        return orderTotal.isGreaterThanOrEqual(discountThreshold);
    }
}