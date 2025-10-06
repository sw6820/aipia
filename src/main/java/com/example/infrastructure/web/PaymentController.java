package com.example.infrastructure.web;

import com.example.domain.Payment;
import com.example.dto.PaymentDto;
import com.example.application.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        log.info("Creating payment for order ID: {}", request.getOrderId());
        try {
            PaymentDto payment = paymentService.createPayment(request.getOrderId(), request.getPaymentMethod());
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for payment creation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable Long id) {
        log.info("Retrieving payment with ID: {}", id);
        return paymentService.getPaymentById(id)
                .map(payment -> ResponseEntity.ok(payment))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("Retrieving payment for order ID: {}", orderId);
        return paymentService.getPaymentByOrderId(orderId)
                .map(payment -> ResponseEntity.ok(payment))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentDto> getPaymentByTransactionId(@PathVariable String transactionId) {
        log.info("Retrieving payment with transaction ID: {}", transactionId);
        return paymentService.getPaymentByTransactionId(transactionId)
                .map(payment -> ResponseEntity.ok(payment))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByStatus(@PathVariable Payment.PaymentStatus status) {
        log.info("Retrieving payments by status: {}", status);
        List<PaymentDto> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PaymentDto>> getPaymentsByMemberId(@PathVariable Long memberId) {
        log.info("Retrieving payments for member ID: {}", memberId);
        List<PaymentDto> payments = paymentService.getPaymentsByMemberId(memberId);
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}/process")
    public ResponseEntity<PaymentDto> processPayment(@PathVariable Long id) {
        log.info("Processing payment with ID: {}", id);
        try {
            PaymentDto payment = paymentService.processPayment(id);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for payment processing: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Invalid state for payment processing: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/fail")
    public ResponseEntity<PaymentDto> failPayment(@PathVariable Long id, @Valid @RequestBody FailPaymentRequest request) {
        log.info("Failing payment with ID: {} - Reason: {}", id, request.getFailureReason());
        try {
            PaymentDto payment = paymentService.failPayment(id, request.getFailureReason());
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for payment failure: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<PaymentDto> refundPayment(@PathVariable Long id) {
        log.info("Refunding payment with ID: {}", id);
        try {
            PaymentDto payment = paymentService.refundPayment(id);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for payment refund: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Invalid state for payment refund: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Data
    public static class CreatePaymentRequest {
        @NotNull(message = "Order ID is required")
        private Long orderId;

        @NotNull(message = "Payment method is required")
        private Payment.PaymentMethod paymentMethod;
    }

    @Data
    public static class FailPaymentRequest {
        @NotBlank(message = "Failure reason is required")
        private String failureReason;
    }
}