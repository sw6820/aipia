package com.example.service;

import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.infrastructure.persistence.PaymentRepository;
import com.example.application.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order testOrder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testOrder = new Order() {
            @Override
            public Long getId() { return 1L; }
            @Override
            public String getOrderNumber() { return "ORD-20240101120000"; }
            @Override
            public BigDecimal getTotalAmount() { return new BigDecimal("100.00"); }
            @Override
            public Payment getPayment() { return null; }
        };

        testPayment = new Payment() {
            @Override
            public Long getId() { return 1L; }
            @Override
            public Order getOrder() { return testOrder; }
            @Override
            public BigDecimal getAmount() { return new BigDecimal("100.00"); }
            @Override
            public Payment.PaymentMethod getPaymentMethod() { return Payment.PaymentMethod.CREDIT_CARD; }
            @Override
            public Payment.PaymentStatus getStatus() { return Payment.PaymentStatus.PENDING; }
            @Override
            public String getTransactionId() { return null; }
            @Override
            public String getFailureReason() { return null; }
        };
    }

    @Test
    @DisplayName("결제 생성 성공")
    void createPayment_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        var result = paymentService.createPayment(1L, Payment.PaymentMethod.CREDIT_CARD);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        
        verify(orderRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 결제 생성 실패")
    void createPayment_OrderNotFound_Failure() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(999L, Payment.PaymentMethod.CREDIT_CARD))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
        
        verify(orderRepository).findById(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("ID로 결제 조회 성공")
    void getPaymentById_Success() {
        // Given
        when(paymentRepository.findByIdWithOrder(1L)).thenReturn(Optional.of(testPayment));

        // When
        var result = paymentService.getPaymentById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualTo(new BigDecimal("100.00"));
        
        verify(paymentRepository).findByIdWithOrder(1L);
    }

    @Test
    @DisplayName("주문 ID로 결제 조회 성공")
    void getPaymentByOrderId_Success() {
        // Given
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(testPayment));

        // When
        var result = paymentService.getPaymentByOrderId(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(1L);
        
        verify(paymentRepository).findByOrderId(1L);
    }

    @Test
    @DisplayName("결제 처리 성공")
    void processPayment_Success() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        var result = paymentService.processPayment(1L);

        // Then
        assertThat(result).isNotNull();
        
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 실패 처리 성공")
    void failPayment_Success() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // When
        var result = paymentService.failPayment(1L, "Insufficient funds");

        // Then
        assertThat(result).isNotNull();
        
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 환불 성공")
    void refundPayment_Success() {
        // Given - Create a real completed payment
        Payment completedPayment = Payment.builder()
                .order(testOrder)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .build();
        completedPayment.process("TXN123456789"); // Set status to COMPLETED

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(completedPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);

        // When
        var result = paymentService.refundPayment(1L);

        // Then
        assertThat(result).isNotNull();
        
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }
}