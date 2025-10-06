package com.example.domain.specification;

import com.example.domain.Order;
import com.example.domain.valueobject.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Specifications for Order domain objects.
 * Encapsulates business rules related to order validation and filtering.
 */
public class OrderSpecifications {
    
    /**
     * Specification for pending orders.
     */
    public static Specification<Order> isPending() {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                return order.getStatus() == Order.OrderStatus.PENDING;
            }
        };
    }
    
    /**
     * Specification for completed orders.
     */
    public static Specification<Order> isCompleted() {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                return order.getStatus() == Order.OrderStatus.COMPLETED;
            }
        };
    }
    
    /**
     * Specification for cancelled orders.
     */
    public static Specification<Order> isCancelled() {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                return order.getStatus() == Order.OrderStatus.CANCELLED;
            }
        };
    }
    
    /**
     * Specification for orders with a minimum amount.
     */
    public static Specification<Order> hasMinimumAmount(BigDecimal minimumAmount) {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                Money orderAmount = Money.krw(order.getTotalAmount());
                Money minimum = Money.krw(minimumAmount);
                return orderAmount.isGreaterThanOrEqual(minimum);
            }
        };
    }
    
    /**
     * Specification for orders with a maximum amount.
     */
    public static Specification<Order> hasMaximumAmount(BigDecimal maximumAmount) {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                Money orderAmount = Money.krw(order.getTotalAmount());
                Money maximum = Money.krw(maximumAmount);
                return orderAmount.isGreaterThanOrEqual(Money.krw(BigDecimal.ZERO)) && 
                       !orderAmount.isGreaterThan(maximum);
            }
        };
    }
    
    /**
     * Specification for orders within a price range.
     */
    public static Specification<Order> hasAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return hasMinimumAmount(minAmount).and(hasMaximumAmount(maxAmount));
    }
    
    /**
     * Specification for orders created after a specific date.
     */
    public static Specification<Order> createdAfter(LocalDateTime date) {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                return order.getCreatedAt() != null && 
                       order.getCreatedAt().isAfter(date);
            }
        };
    }
    
    /**
     * Specification for orders created before a specific date.
     */
    public static Specification<Order> createdBefore(LocalDateTime date) {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                return order.getCreatedAt() != null && 
                       order.getCreatedAt().isBefore(date);
            }
        };
    }
    
    /**
     * Specification for orders created within a date range.
     */
    public static Specification<Order> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return createdAfter(startDate).and(createdBefore(endDate));
    }
    
    /**
     * Specification for orders with items.
     */
    public static Specification<Order> hasItems() {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                return order.getOrderItems() != null && !order.getOrderItems().isEmpty();
            }
        };
    }
    
    /**
     * Specification for orders with a minimum number of items.
     */
    public static Specification<Order> hasMinimumItems(int minimumItems) {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                return order.getOrderItems() != null && 
                       order.getOrderItems().size() >= minimumItems;
            }
        };
    }
    
    /**
     * Specification for orders with payment.
     */
    public static Specification<Order> hasPayment() {
        return new Specification<Order>() {
            @Override
            public boolean isSatisfiedBy(Order order) {
                return order.getPayment() != null;
            }
        };
    }
    
    /**
     * Specification for high-value orders (above 100,000 KRW).
     */
    public static Specification<Order> isHighValue() {
        return hasMinimumAmount(BigDecimal.valueOf(100000));
    }
    
    /**
     * Specification for bulk orders (more than 5 items).
     */
    public static Specification<Order> isBulkOrder() {
        return hasMinimumItems(5);
    }
    
    /**
     * Specification for recent orders (created within last 30 days).
     */
    public static Specification<Order> isRecent() {
        return createdAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Specification for orders that can be cancelled.
     */
    public static Specification<Order> canBeCancelled() {
        return isPending().and(hasPayment().not());
    }
    
    /**
     * Specification for orders that can be completed.
     */
    public static Specification<Order> canBeCompleted() {
        return isPending().and(hasPayment());
    }
}