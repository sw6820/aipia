package com.example.testdata;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 테스트용 고정 데이터를 제공하는 픽스처 클래스
 */
public class TestDataFixtures {

    /**
     * 기본 테스트 회원 데이터
     */
    public static class Members {
        public static final String DEFAULT_EMAIL = "test@example.com";
        public static final String DEFAULT_NAME = "Test User";
        public static final String DEFAULT_PHONE = "010-1234-5678";

        public static Member createDefault() {
            return Member.builder()
                    .email(DEFAULT_EMAIL)
                    .name(DEFAULT_NAME)
                    .phoneNumber(DEFAULT_PHONE)
                    .build();
        }

        public static Member createWithEmail(String email) {
            return Member.builder()
                    .email(email)
                    .name(DEFAULT_NAME)
                    .phoneNumber(DEFAULT_PHONE)
                    .build();
        }

        public static Member createWithName(String name) {
            return Member.builder()
                    .email(DEFAULT_EMAIL)
                    .name(name)
                    .phoneNumber(DEFAULT_PHONE)
                    .build();
        }

        public static Member createActive() {
            Member member = createDefault();
            member.activate();
            return member;
        }

        public static Member createInactive() {
            Member member = createDefault();
            member.deactivate();
            return member;
        }
    }

    /**
     * 기본 테스트 주문 데이터
     */
    public static class Orders {
        public static final String DEFAULT_ORDER_NUMBER = "ORD-TEST-001";
        public static final BigDecimal DEFAULT_TOTAL_AMOUNT = BigDecimal.valueOf(100.00);

        public static Order createDefault(Member member) {
            return Order.builder()
                    .orderNumber(DEFAULT_ORDER_NUMBER)
                    .member(member)
                    .totalAmount(DEFAULT_TOTAL_AMOUNT)
                    .build();
        }

        public static Order createWithOrderNumber(String orderNumber, Member member) {
            return Order.builder()
                    .orderNumber(orderNumber)
                    .member(member)
                    .totalAmount(DEFAULT_TOTAL_AMOUNT)
                    .build();
        }

        public static Order createWithAmount(BigDecimal amount, Member member) {
            return Order.builder()
                    .orderNumber(DEFAULT_ORDER_NUMBER)
                    .member(member)
                    .totalAmount(amount)
                    .build();
        }

        public static Order createPending(Member member) {
            return createDefault(member);
        }

        public static Order createConfirmed(Member member) {
            Order order = createDefault(member);
            order.confirm();
            return order;
        }

        public static Order createCompleted(Member member) {
            Order order = createDefault(member);
            order.confirm();
            order.complete();
            return order;
        }

        public static Order createCancelled(Member member) {
            Order order = createDefault(member);
            order.cancel();
            return order;
        }
    }

    /**
     * 기본 테스트 결제 데이터
     */
    public static class Payments {
        public static final Payment.PaymentMethod DEFAULT_PAYMENT_METHOD = Payment.PaymentMethod.CREDIT_CARD;
        public static final String DEFAULT_TRANSACTION_ID = "TXN-TEST-001";
        public static final String DEFAULT_FAILURE_REASON = "Test failure reason";

        public static Payment createDefault(Order order) {
            return Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod(DEFAULT_PAYMENT_METHOD)
                    .build();
        }

        public static Payment createWithPaymentMethod(Payment.PaymentMethod paymentMethod, Order order) {
            return Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .paymentMethod(paymentMethod)
                    .build();
        }

        public static Payment createPending(Order order) {
            return createDefault(order);
        }

        public static Payment createCompleted(Order order) {
            Payment payment = createDefault(order);
            payment.process(DEFAULT_TRANSACTION_ID);
            return payment;
        }

        public static Payment createFailed(Order order) {
            Payment payment = createDefault(order);
            payment.fail(DEFAULT_FAILURE_REASON);
            return payment;
        }

        public static Payment createRefunded(Order order) {
            Payment payment = createDefault(order);
            payment.process(DEFAULT_TRANSACTION_ID);
            payment.refund();
            return payment;
        }
    }

    /**
     * 복합 테스트 시나리오 데이터
     */
    public static class Scenarios {
        
        /**
         * 완전한 주문 플로우 데이터
         */
        public static class CompleteOrderFlow {
            public static Member createTestMember() {
                return Members.createActive();
            }

            public static Order createTestOrder(Member member) {
                return Orders.createConfirmed(member);
            }

            public static Payment createTestPayment(Order order) {
                return Payments.createCompleted(order);
            }
        }

        /**
         * 결제 실패 시나리오 데이터
         */
        public static class PaymentFailureFlow {
            public static Member createTestMember() {
                return Members.createActive();
            }

            public static Order createTestOrder(Member member) {
                return Orders.createConfirmed(member);
            }

            public static Payment createTestPayment(Order order) {
                return Payments.createFailed(order);
            }
        }

        /**
         * 환불 시나리오 데이터
         */
        public static class RefundFlow {
            public static Member createTestMember() {
                return Members.createActive();
            }

            public static Order createTestOrder(Member member) {
                return Orders.createCompleted(member);
            }

            public static Payment createTestPayment(Order order) {
                return Payments.createRefunded(order);
            }
        }

        /**
         * 대량 데이터 생성용
         */
        public static class BulkData {
            public static List<Member> createMultipleMembers(int count) {
                return IntStream.range(0, count)
                        .mapToObj(i -> Members.createWithEmail("bulk" + i + "@example.com"))
                        .toList();
            }

            public static List<Order> createMultipleOrders(Member member, int count) {
                return IntStream.range(0, count)
                        .mapToObj(i -> Orders.createWithOrderNumber("ORD-BULK-" + i, member))
                        .toList();
            }

            public static List<Payment> createMultiplePayments(List<Order> orders) {
                return orders.stream()
                        .map(Payments::createDefault)
                        .toList();
            }
        }
    }

    /**
     * 테스트용 상수값들
     */
    public static class Constants {
        public static final String VALID_EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        public static final String VALID_PHONE_PATTERN = "^010-\\d{4}-\\d{4}$";
        public static final String VALID_ORDER_NUMBER_PATTERN = "^ORD-\\d{3,}$";
        public static final String VALID_TRANSACTION_ID_PATTERN = "^TXN-\\d{6,}$";

        public static final BigDecimal MIN_ORDER_AMOUNT = BigDecimal.valueOf(0.01);
        public static final BigDecimal MAX_ORDER_AMOUNT = BigDecimal.valueOf(999999.99);
        public static final BigDecimal MIN_PAYMENT_AMOUNT = BigDecimal.valueOf(0.01);
        public static final BigDecimal MAX_PAYMENT_AMOUNT = BigDecimal.valueOf(999999.99);

        public static final int MAX_NAME_LENGTH = 100;
        public static final int MAX_EMAIL_LENGTH = 255;
        public static final int MAX_PHONE_LENGTH = 20;
        public static final int MAX_ORDER_NUMBER_LENGTH = 50;
        public static final int MAX_TRANSACTION_ID_LENGTH = 100;
        public static final int MAX_FAILURE_REASON_LENGTH = 500;
    }

    /**
     * 테스트용 예외 메시지들
     */
    public static class ErrorMessages {
        public static final String MEMBER_NOT_FOUND = "회원을 찾을 수 없습니다";
        public static final String ORDER_NOT_FOUND = "주문을 찾을 수 없습니다";
        public static final String PAYMENT_NOT_FOUND = "결제를 찾을 수 없습니다";
        public static final String DUPLICATE_EMAIL = "이미 존재하는 이메일입니다";
        public static final String DUPLICATE_ORDER_NUMBER = "이미 존재하는 주문번호입니다";
        public static final String INVALID_ORDER_STATUS_TRANSITION = "유효하지 않은 주문 상태 변경입니다";
        public static final String INVALID_PAYMENT_STATUS_TRANSITION = "유효하지 않은 결제 상태 변경입니다";
        public static final String PAYMENT_ALREADY_PROCESSED = "이미 처리된 결제입니다";
        public static final String ORDER_ALREADY_CANCELLED = "이미 취소된 주문입니다";
        public static final String ORDER_ALREADY_COMPLETED = "이미 완료된 주문입니다";
        public static final String PAYMENT_NOT_COMPLETED = "완료되지 않은 결제입니다";
        public static final String PAYMENT_ALREADY_REFUNDED = "이미 환불된 결제입니다";
    }
}