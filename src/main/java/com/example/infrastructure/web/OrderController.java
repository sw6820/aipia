package com.example.infrastructure.web;

import com.example.domain.Order;
import com.example.dto.OrderDto;
import com.example.application.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for member ID: {}", request.getMemberId());
        
        try {
            // Convert controller OrderItemRequest to service OrderItemRequest
            List<OrderService.OrderItemRequest> serviceOrderItems = request.getOrderItems().stream()
                    .map(item -> new OrderService.OrderItemRequest(
                            item.getProductName(),
                            item.getProductDescription(),
                            item.getQuantity(),
                            item.getUnitPrice()
                    ))
                    .toList();
            
            OrderDto order = orderService.createOrder(request.getMemberId(), serviceOrderItems);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for order creation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        log.info("Retrieving order with ID: {}", id);
        return orderService.getOrderById(id)
                .map(order -> ResponseEntity.ok(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("Retrieving order with order number: {}", orderNumber);
        return orderService.getOrderByOrderNumber(orderNumber)
                .map(order -> ResponseEntity.ok(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<OrderDto>> getOrdersByMemberId(@PathVariable Long memberId) {
        log.info("Retrieving orders for member ID: {}", memberId);
        List<OrderDto> orders = orderService.getOrdersByMemberId(memberId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        log.info("Retrieving orders by status: {}", status);
        List<OrderDto> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<OrderDto> confirmOrder(@PathVariable Long id) {
        log.info("Confirming order with ID: {}", id);
        try {
            OrderDto order = orderService.confirmOrder(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for order confirmation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Invalid state for order confirmation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id) {
        log.info("Cancelling order with ID: {}", id);
        try {
            OrderDto order = orderService.cancelOrder(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for order cancellation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Invalid state for order cancellation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<OrderDto> completeOrder(@PathVariable Long id) {
        log.info("Completing order with ID: {}", id);
        try {
            OrderDto order = orderService.completeOrder(id);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for order completion: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.warn("Invalid state for order completion: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Data
    public static class CreateOrderRequest {
        @NotNull(message = "Member ID is required")
        private Long memberId;

        @Valid
        private List<OrderItemRequest> orderItems;

        @Data
        public static class OrderItemRequest {
            @NotBlank(message = "Product name is required")
            private String productName;

            @NotBlank(message = "Product description is required")
            private String productDescription;

            @NotNull(message = "Quantity is required")
            @Min(value = 1, message = "Quantity must be at least 1")
            private Integer quantity;

            @NotNull(message = "Unit price is required")
            @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
            private BigDecimal unitPrice;
        }
    }
}