package com.example.testdata;

import com.example.domain.Member;
import com.example.domain.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderTestDataBuilder {

    private String orderNumber = "ORD-001";
    private Member member;
    private BigDecimal totalAmount = BigDecimal.valueOf(100.00);
    private Order.OrderStatus status = Order.OrderStatus.PENDING;

    public static OrderTestDataBuilder anOrder() {
        return new OrderTestDataBuilder();
    }

    public OrderTestDataBuilder withOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    public OrderTestDataBuilder withMember(Member member) {
        this.member = member;
        return this;
    }

    public OrderTestDataBuilder withTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public OrderTestDataBuilder withStatus(Order.OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderTestDataBuilder pending() {
        this.status = Order.OrderStatus.PENDING;
        return this;
    }

    public OrderTestDataBuilder confirmed() {
        this.status = Order.OrderStatus.CONFIRMED;
        return this;
    }

    public OrderTestDataBuilder cancelled() {
        this.status = Order.OrderStatus.CANCELLED;
        return this;
    }

    public OrderTestDataBuilder completed() {
        this.status = Order.OrderStatus.COMPLETED;
        return this;
    }

    public Order build() {
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .member(member)
                .totalAmount(totalAmount)
                .build();
        
        // 상태 설정
        switch (status) {
            case CONFIRMED:
                order.confirm();
                break;
            case CANCELLED:
                order.cancel();
                break;
            case COMPLETED:
                order.confirm();
                order.complete();
                break;
            case PENDING:
            default:
                // 기본 상태는 PENDING
                break;
        }
        
        return order;
    }

    public static List<Order> createMultipleOrders(Member member, int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            orders.add(anOrder()
                    .withOrderNumber("ORD-" + String.format("%03d", i + 1))
                    .withMember(member)
                    .withTotalAmount(BigDecimal.valueOf(100.00 + i * 10))
                    .build());
        }
        return orders;
    }

    public static Order createPendingOrder(Member member) {
        return anOrder().withMember(member).pending().build();
    }

    public static Order createConfirmedOrder(Member member) {
        return anOrder().withMember(member).confirmed().build();
    }

    public static Order createCancelledOrder(Member member) {
        return anOrder().withMember(member).cancelled().build();
    }

    public static Order createCompletedOrder(Member member) {
        return anOrder().withMember(member).completed().build();
    }

    public static Order createOrderWithAmount(Member member, BigDecimal amount) {
        return anOrder().withMember(member).withTotalAmount(amount).build();
    }
}