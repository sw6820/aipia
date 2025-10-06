package com.example.testdata;

import com.example.domain.Order;
import com.example.domain.Payment;

import java.math.BigDecimal;

public class PaymentTestDataBuilder {

    private Order order;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.CREDIT_CARD;
    private Payment.PaymentStatus status = Payment.PaymentStatus.PENDING;
    private String transactionId;
    private String failureReason;

    public static PaymentTestDataBuilder aPayment() {
        return new PaymentTestDataBuilder();
    }

    public PaymentTestDataBuilder withOrder(Order order) {
        this.order = order;
        this.amount = order.getTotalAmount(); // 주문 총액으로 자동 설정
        return this;
    }

    public PaymentTestDataBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public PaymentTestDataBuilder withPaymentMethod(Payment.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public PaymentTestDataBuilder withStatus(Payment.PaymentStatus status) {
        this.status = status;
        return this;
    }

    public PaymentTestDataBuilder withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public PaymentTestDataBuilder withFailureReason(String failureReason) {
        this.failureReason = failureReason;
        return this;
    }

    public PaymentTestDataBuilder creditCard() {
        this.paymentMethod = Payment.PaymentMethod.CREDIT_CARD;
        return this;
    }

    public PaymentTestDataBuilder debitCard() {
        this.paymentMethod = Payment.PaymentMethod.DEBIT_CARD;
        return this;
    }

    public PaymentTestDataBuilder bankTransfer() {
        this.paymentMethod = Payment.PaymentMethod.BANK_TRANSFER;
        return this;
    }

    public PaymentTestDataBuilder cash() {
        this.paymentMethod = Payment.PaymentMethod.CASH;
        return this;
    }

    public PaymentTestDataBuilder pending() {
        this.status = Payment.PaymentStatus.PENDING;
        return this;
    }

    public PaymentTestDataBuilder completed() {
        this.status = Payment.PaymentStatus.COMPLETED;
        return this;
    }

    public PaymentTestDataBuilder failed() {
        this.status = Payment.PaymentStatus.FAILED;
        return this;
    }

    public PaymentTestDataBuilder refunded() {
        this.status = Payment.PaymentStatus.REFUNDED;
        return this;
    }

    public Payment build() {
        Payment payment = Payment.builder()
                .order(order)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .build();
        
        // 상태 설정
        switch (status) {
            case COMPLETED:
                payment.process(transactionId != null ? transactionId : "TXN-TEST-001");
                break;
            case FAILED:
                payment.fail(failureReason != null ? failureReason : "Test failure");
                break;
            case REFUNDED:
                payment.process("TXN-TEST-001");
                payment.refund();
                break;
            case PENDING:
            default:
                // 기본 상태는 PENDING
                break;
        }
        
        return payment;
    }

    public static Payment createPendingPayment(Order order) {
        return aPayment().withOrder(order).pending().build();
    }

    public static Payment createCompletedPayment(Order order) {
        return aPayment().withOrder(order).completed().withTransactionId("TXN-COMPLETED-001").build();
    }

    public static Payment createFailedPayment(Order order) {
        return aPayment().withOrder(order).failed().withFailureReason("Insufficient funds").build();
    }

    public static Payment createRefundedPayment(Order order) {
        return aPayment().withOrder(order).refunded().withTransactionId("TXN-REFUNDED-001").build();
    }

    public static Payment createCreditCardPayment(Order order) {
        return aPayment().withOrder(order).creditCard().build();
    }

    public static Payment createDebitCardPayment(Order order) {
        return aPayment().withOrder(order).debitCard().build();
    }

    public static Payment createBankTransferPayment(Order order) {
        return aPayment().withOrder(order).bankTransfer().build();
    }

    public static Payment createCashPayment(Order order) {
        return aPayment().withOrder(order).cash().build();
    }
}