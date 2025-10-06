package com.example.application.service;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.OrderItem;
import com.example.dto.OrderDto;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.persistence.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public OrderDto createOrder(Long memberId, List<OrderItemRequest> orderItems) {
        log.info("Creating order for member ID: {}", memberId);
        
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be null or empty");
        }
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        String orderNumber = generateOrderNumber();
        BigDecimal totalAmount = calculateTotalAmount(orderItems);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .member(member)
                .totalAmount(totalAmount)
                .build();

        // Add order items
        for (OrderItemRequest itemRequest : orderItems) {
            OrderItem orderItem = OrderItem.builder()
                    .productName(itemRequest.getProductName())
                    .productDescription(itemRequest.getProductDescription())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .build();
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        member.addOrder(savedOrder);
        
        log.info("Order created successfully with ID: {} and order number: {}", 
                savedOrder.getId(), savedOrder.getOrderNumber());
        
        return OrderDto.from(savedOrder);
    }

    public Optional<OrderDto> getOrderById(Long id) {
        log.info("Retrieving order with ID: {}", id);
        return orderRepository.findByIdWithOrderItemsAndPayment(id)
                .map(OrderDto::from);
    }

    public Optional<OrderDto> getOrderByOrderNumber(String orderNumber) {
        log.info("Retrieving order with order number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .map(OrderDto::from);
    }

    public List<OrderDto> getOrdersByMemberId(Long memberId) {
        log.info("Retrieving orders for member ID: {}", memberId);
        return orderRepository.findByMemberId(memberId).stream()
                .map(OrderDto::from)
                .toList();
    }

    public List<OrderDto> getOrdersByStatus(Order.OrderStatus status) {
        log.info("Retrieving orders by status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(OrderDto::from)
                .toList();
    }

    @Transactional
    public OrderDto confirmOrder(Long orderId) {
        log.info("Confirming order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        
        order.confirm();
        Order updatedOrder = orderRepository.save(order);
        log.info("Order confirmed successfully with ID: {}", orderId);
        
        return OrderDto.from(updatedOrder);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        
        order.cancel();
        Order updatedOrder = orderRepository.save(order);
        log.info("Order cancelled successfully with ID: {}", orderId);
        
        return OrderDto.from(updatedOrder);
    }

    @Transactional
    public OrderDto completeOrder(Long orderId) {
        log.info("Completing order with ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        
        order.complete();
        Order updatedOrder = orderRepository.save(order);
        log.info("Order completed successfully with ID: {}", orderId);
        
        return OrderDto.from(updatedOrder);
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal calculateTotalAmount(List<OrderItemRequest> orderItems) {
        return orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static class OrderItemRequest {
        private String productName;
        private String productDescription;
        private Integer quantity;
        private BigDecimal unitPrice;

        public OrderItemRequest() {}

        public OrderItemRequest(String productName, String productDescription, Integer quantity, BigDecimal unitPrice) {
            this.productName = productName;
            this.productDescription = productDescription;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // Getters and setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getProductDescription() { return productDescription; }
        public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}