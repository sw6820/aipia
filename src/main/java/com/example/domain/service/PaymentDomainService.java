package com.example.domain.service;

import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.domain.valueobject.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain service for complex payment-related business logic.
 * Contains business rules that don't naturally belong to a single entity.
 */
@Service
@Slf4j
public class PaymentDomainService {
    
    /**
     * Validates that a payment amount matches the order total.
     * This is a critical business rule for financial integrity.
     */
    public boolean validatePaymentAmount(Payment payment) {
        log.debug("Validating payment amount for payment: {}", payment.getId());
        
        Order order = payment.getOrder();
        Money orderTotal = Money.krw(order.getTotalAmount());
        Money paymentAmount = Money.krw(payment.getAmount());
        
        boolean isValid = orderTotal.equals(paymentAmount);
        
        if (!isValid) {
            log.warn("Payment amount mismatch for payment {}: order total {}, payment amount {}", 
                    payment.getId(), orderTotal, paymentAmount);
        }
        
        return isValid;
    }
    
    /**
     * Determines if a payment can be processed based on business rules.
     */
    public boolean canProcessPayment(Payment payment) {
        log.debug("Checking if payment can be processed: {}", payment.getId());
        
        // Business rule: Only pending payments can be processed
        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            log.debug("Payment {} cannot be processed: status is {}", 
                     payment.getId(), payment.getStatus());
            return false;
        }
        
        // Business rule: Payment amount must match order total
        if (!validatePaymentAmount(payment)) {
            log.debug("Payment {} cannot be processed: amount mismatch", payment.getId());
            return false;
        }
        
        // Business rule: Order must be in pending status
        if (payment.getOrder().getStatus() != Order.OrderStatus.PENDING) {
            log.debug("Payment {} cannot be processed: order status is {}", 
                     payment.getId(), payment.getOrder().getStatus());
            return false;
        }
        
        log.debug("Payment {} can be processed", payment.getId());
        return true;
    }
    
    /**
     * Determines if a payment can be refunded based on business rules.
     */
    public boolean canRefundPayment(Payment payment) {
        log.debug("Checking if payment can be refunded: {}", payment.getId());
        
        // Business rule: Only completed payments can be refunded
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            log.debug("Payment {} cannot be refunded: status is {}", 
                     payment.getId(), payment.getStatus());
            return false;
        }
        
        // Business rule: Order must be completed
        if (payment.getOrder().getStatus() != Order.OrderStatus.COMPLETED) {
            log.debug("Payment {} cannot be refunded: order not completed", payment.getId());
            return false;
        }
        
        log.debug("Payment {} can be refunded", payment.getId());
        return true;
    }
    
    /**
     * Calculates the total amount of all payments for a member.
     */
    public Money calculateMemberTotalPayments(List<Payment> payments) {
        log.debug("Calculating total payments for member with {} payments", payments.size());
        
        BigDecimal total = payments.stream()
                .filter(payment -> payment.getStatus() == Payment.PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Money totalMoney = Money.krw(total);
        log.debug("Calculated total payments: {}", totalMoney);
        
        return totalMoney;
    }
    
    /**
     * Determines if a payment method is supported for a given amount.
     */
    public boolean isPaymentMethodSupported(Payment.PaymentMethod method, Money amount) {
        log.debug("Checking if payment method {} is supported for amount {}", method, amount);
        
        // Business rule: Credit card has a minimum amount requirement
        if (method == Payment.PaymentMethod.CREDIT_CARD) {
            Money minimumAmount = Money.krw(BigDecimal.valueOf(1000)); // 1000 KRW minimum
            boolean supported = amount.isGreaterThanOrEqual(minimumAmount);
            
            if (!supported) {
                log.debug("Credit card not supported for amount {}: minimum required {}", 
                         amount, minimumAmount);
            }
            
            return supported;
        }
        
        // Business rule: Bank transfer has a maximum amount limit
        if (method == Payment.PaymentMethod.BANK_TRANSFER) {
            Money maximumAmount = Money.krw(BigDecimal.valueOf(10000000)); // 10M KRW maximum
            boolean supported = amount.isGreaterThanOrEqual(Money.krw(BigDecimal.ZERO)) && 
                               amount.isGreaterThanOrEqual(maximumAmount) == false;
            
            if (!supported) {
                log.debug("Bank transfer not supported for amount {}: maximum allowed {}", 
                         amount, maximumAmount);
            }
            
            return supported;
        }
        
        // Business rule: Cash has no restrictions
        if (method == Payment.PaymentMethod.CASH) {
            return true;
        }
        
        log.debug("Payment method {} is supported for amount {}", method, amount);
        return true;
    }
    
    /**
     * Calculates processing fee for a payment method and amount.
     */
    public Money calculateProcessingFee(Payment.PaymentMethod method, Money amount) {
        log.debug("Calculating processing fee for method {} and amount {}", method, amount);
        
        Money fee;
        
        switch (method) {
            case CREDIT_CARD:
                // 2.5% fee for credit card
                fee = amount.multiply(BigDecimal.valueOf(0.025));
                break;
            case BANK_TRANSFER:
                // Fixed 500 KRW fee for bank transfer
                fee = Money.krw(BigDecimal.valueOf(500));
                break;
            case CASH:
                // No fee for cash
                fee = Money.krw(BigDecimal.ZERO);
                break;
            default:
                fee = Money.krw(BigDecimal.ZERO);
        }
        
        log.debug("Calculated processing fee: {}", fee);
        return fee;
    }
}