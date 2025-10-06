package com.example.application.service;

import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.dto.PaymentDto;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentDto createPayment(Long orderId, Payment.PaymentMethod paymentMethod) {
        log.info("Creating payment for order ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (order.getPayment() != null) {
            throw new IllegalArgumentException("Payment already exists for order ID: " + orderId);
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .paymentMethod(paymentMethod)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        order.setPayment(savedPayment);
        savedPayment.setOrder(order);
        
        log.info("Payment created successfully with ID: {} for order ID: {}", 
                savedPayment.getId(), orderId);
        
        return PaymentDto.from(savedPayment);
    }

    public Optional<PaymentDto> getPaymentById(Long id) {
        log.info("Retrieving payment with ID: {}", id);
        return paymentRepository.findByIdWithOrder(id)
                .map(PaymentDto::from);
    }

    public Optional<PaymentDto> getPaymentByOrderId(Long orderId) {
        log.info("Retrieving payment for order ID: {}", orderId);
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentDto::from);
    }

    public Optional<PaymentDto> getPaymentByTransactionId(String transactionId) {
        log.info("Retrieving payment with transaction ID: {}", transactionId);
        return paymentRepository.findByTransactionId(transactionId)
                .map(PaymentDto::from);
    }

    public List<PaymentDto> getPaymentsByStatus(Payment.PaymentStatus status) {
        log.info("Retrieving payments by status: {}", status);
        return paymentRepository.findByStatus(status).stream()
                .map(PaymentDto::from)
                .toList();
    }

    public List<PaymentDto> getPaymentsByMemberId(Long memberId) {
        log.info("Retrieving payments for member ID: {}", memberId);
        return paymentRepository.findByMemberId(memberId).stream()
                .map(PaymentDto::from)
                .toList();
    }

    @Transactional
    public PaymentDto processPayment(Long paymentId) {
        log.info("Processing payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        // Allow re-processing of completed payments
        if (payment.getStatus() == Payment.PaymentStatus.FAILED) {
            throw new IllegalStateException("Failed payments cannot be processed");
        }

        // Simulate payment processing
        String transactionId = generateTransactionId();
        payment.process(transactionId);
        
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully with transaction ID: {}", transactionId);
        
        return PaymentDto.from(updatedPayment);
    }

    @Transactional
    public PaymentDto failPayment(Long paymentId, String failureReason) {
        log.info("Failing payment with ID: {} - Reason: {}", paymentId, failureReason);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        payment.fail(failureReason);
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment failed successfully with ID: {}", paymentId);
        
        return PaymentDto.from(updatedPayment);
    }

    @Transactional
    public PaymentDto refundPayment(Long paymentId) {
        log.info("Refunding payment with ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));

        payment.refund();
        Payment updatedPayment = paymentRepository.save(payment);
        log.info("Payment refunded successfully with ID: {}", paymentId);
        
        return PaymentDto.from(updatedPayment);
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}