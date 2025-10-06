package com.example.controller;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.persistence.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderController 통합 테스트")
class OrderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Member testMember;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        testMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        testMember = memberRepository.save(testMember);
    }

    @Nested
    @DisplayName("주문 생성 API 테스트")
    class CreateOrderTest {

        @Test
        @DisplayName("정상적인 주문 생성 성공")
        void createOrder_Success() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.OrderController.CreateOrderRequest();
            request.setMemberId(testMember.getId());
            
            var orderItem = new com.example.infrastructure.web.OrderController.CreateOrderRequest.OrderItemRequest();
            orderItem.setProductName("Test Product");
            orderItem.setProductDescription("Test Description");
            orderItem.setQuantity(2);
            orderItem.setUnitPrice(BigDecimal.valueOf(50.00));
            
            request.setOrderItems(List.of(orderItem));

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderNumber").isNotEmpty())
                    .andExpect(jsonPath("$.memberId").value(testMember.getId()))
                    .andExpect(jsonPath("$.memberName").value("Test User"))
                    .andExpect(jsonPath("$.totalAmount").value(100.00))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.orderItems").isArray())
                    .andExpect(jsonPath("$.orderItems[0].productName").value("Test Product"))
                    .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                    .andExpect(jsonPath("$.orderItems[0].unitPrice").value(50.00));
        }

        @Test
        @DisplayName("존재하지 않는 회원으로 주문 생성 실패")
        void createOrder_MemberNotFound_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.OrderController.CreateOrderRequest();
            request.setMemberId(999L);
            
            var orderItem = new com.example.infrastructure.web.OrderController.CreateOrderRequest.OrderItemRequest();
            orderItem.setProductName("Test Product");
            orderItem.setProductDescription("Test Description");
            orderItem.setQuantity(1);
            orderItem.setUnitPrice(BigDecimal.valueOf(50.00));
            
            request.setOrderItems(List.of(orderItem));

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("빈 주문 아이템으로 주문 생성 실패")
        void createOrder_EmptyOrderItems_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.OrderController.CreateOrderRequest();
            request.setMemberId(testMember.getId());
            request.setOrderItems(List.of());

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 주문 아이템 정보로 주문 생성 실패")
        void createOrder_InvalidOrderItem_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.OrderController.CreateOrderRequest();
            request.setMemberId(testMember.getId());
            
            var orderItem = new com.example.infrastructure.web.OrderController.CreateOrderRequest.OrderItemRequest();
            orderItem.setProductName(""); // 빈 상품명
            orderItem.setProductDescription("Test Description");
            orderItem.setQuantity(0); // 잘못된 수량
            orderItem.setUnitPrice(BigDecimal.valueOf(-10.00)); // 음수 가격
            
            request.setOrderItems(List.of(orderItem));

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("여러 주문 아이템으로 주문 생성 성공")
        void createOrder_MultipleOrderItems_Success() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.OrderController.CreateOrderRequest();
            request.setMemberId(testMember.getId());
            
            var orderItem1 = new com.example.infrastructure.web.OrderController.CreateOrderRequest.OrderItemRequest();
            orderItem1.setProductName("Product 1");
            orderItem1.setProductDescription("Description 1");
            orderItem1.setQuantity(2);
            orderItem1.setUnitPrice(BigDecimal.valueOf(30.00));
            
            var orderItem2 = new com.example.infrastructure.web.OrderController.CreateOrderRequest.OrderItemRequest();
            orderItem2.setProductName("Product 2");
            orderItem2.setProductDescription("Description 2");
            orderItem2.setQuantity(1);
            orderItem2.setUnitPrice(BigDecimal.valueOf(40.00));
            
            request.setOrderItems(List.of(orderItem1, orderItem2));

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalAmount").value(100.00))
                    .andExpect(jsonPath("$.orderItems").isArray())
                    .andExpect(jsonPath("$.orderItems.length()").value(2));
        }
    }

    @Nested
    @DisplayName("주문 조회 API 테스트")
    class GetOrderTest {

        private Order testOrder;

        @BeforeEach
        void setUp() {
            testOrder = Order.builder()
                    .orderNumber("ORD-TEST-001")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(150.00))
                    .build();
            testOrder = orderRepository.save(testOrder);
        }

        @Test
        @DisplayName("ID로 주문 조회 성공")
        void getOrderById_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/{id}", testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testOrder.getId()))
                    .andExpect(jsonPath("$.orderNumber").value("ORD-TEST-001"))
                    .andExpect(jsonPath("$.memberId").value(testMember.getId()))
                    .andExpect(jsonPath("$.memberName").value("Test User"))
                    .andExpect(jsonPath("$.totalAmount").value(150.00))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회")
        void getOrderById_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/{id}", 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("주문번호로 주문 조회 성공")
        void getOrderByOrderNumber_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/order-number/{orderNumber}", "ORD-TEST-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderNumber").value("ORD-TEST-001"))
                    .andExpect(jsonPath("$.memberId").value(testMember.getId()));
        }

        @Test
        @DisplayName("존재하지 않는 주문번호로 조회")
        void getOrderByOrderNumber_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/order-number/{orderNumber}", "ORD-NONEXISTENT"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("회원별 주문 조회 성공")
        void getOrdersByMemberId_Success() throws Exception {
            // Given
            Order order2 = Order.builder()
                    .orderNumber("ORD-TEST-002")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            orderRepository.save(order2);

            // When & Then
            mockMvc.perform(get("/api/orders/member/{memberId}", testMember.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].memberId").value(testMember.getId()))
                    .andExpect(jsonPath("$[1].memberId").value(testMember.getId()));
        }

        @Test
        @DisplayName("존재하지 않는 회원의 주문 조회")
        void getOrdersByMemberId_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/member/{memberId}", 999L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("주문 상태 변경 API 테스트")
    class OrderStatusChangeTest {

        private Order testOrder;

        @BeforeEach
        void setUp() {
            testOrder = Order.builder()
                    .orderNumber("ORD-STATUS-001")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();
            testOrder = orderRepository.save(testOrder);
        }

        @Test
        @DisplayName("주문 확인 성공")
        void confirmOrder_Success() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/orders/{id}/confirm", testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("주문 취소 성공")
        void cancelOrder_Success() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/orders/{id}/cancel", testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("주문 완료 성공")
        void completeOrder_Success() throws Exception {
            // Given
            testOrder.confirm();
            testOrder = orderRepository.save(testOrder);

            // When & Then
            mockMvc.perform(put("/api/orders/{id}/complete", testOrder.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("존재하지 않는 주문 상태 변경 실패")
        void changeOrderStatus_NotFound() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/orders/{id}/confirm", 999L))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("취소된 주문 완료 실패")
        void completeCancelledOrder_Failure() throws Exception {
            // Given
            testOrder.cancel();
            testOrder = orderRepository.save(testOrder);

            // When & Then
            mockMvc.perform(put("/api/orders/{id}/complete", testOrder.getId()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("상태별 주문 조회 API 테스트")
    class GetOrdersByStatusTest {

        @BeforeEach
        void setUp() {
            // 다양한 상태의 주문 생성
            Order pendingOrder = Order.builder()
                    .orderNumber("ORD-PENDING")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();
            orderRepository.save(pendingOrder);

            Order confirmedOrder = Order.builder()
                    .orderNumber("ORD-CONFIRMED")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            confirmedOrder.confirm();
            orderRepository.save(confirmedOrder);

            Order cancelledOrder = Order.builder()
                    .orderNumber("ORD-CANCELLED")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(300.00))
                    .build();
            cancelledOrder.cancel();
            orderRepository.save(cancelledOrder);
        }

        @Test
        @DisplayName("대기 상태 주문 조회")
        void getOrdersByStatus_Pending() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/status/PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("확인된 주문 조회")
        void getOrdersByStatus_Confirmed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/status/CONFIRMED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("취소된 주문 조회")
        void getOrdersByStatus_Cancelled() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/status/CANCELLED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status").value("CANCELLED"));
        }

        @Test
        @DisplayName("완료된 주문 조회")
        void getOrdersByStatus_Completed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/orders/status/COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("주문 검증 테스트")
    class OrderValidationTest {

        @Test
        @DisplayName("null 회원 ID로 주문 생성 실패")
        void createOrder_NullMemberId_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.OrderController.CreateOrderRequest();
            request.setMemberId(null);
            
            var orderItem = new com.example.infrastructure.web.OrderController.CreateOrderRequest.OrderItemRequest();
            orderItem.setProductName("Test Product");
            orderItem.setProductDescription("Test Description");
            orderItem.setQuantity(1);
            orderItem.setUnitPrice(BigDecimal.valueOf(50.00));
            
            request.setOrderItems(List.of(orderItem));

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 JSON 형식으로 주문 생성 실패")
        void createOrder_InvalidJson_Failure() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Content-Type이 없는 주문 생성 실패")
        void createOrder_NoContentType_Failure() throws Exception {
            // Given
            var request = new com.example.infrastructure.web.OrderController.CreateOrderRequest();
            request.setMemberId(testMember.getId());
            
            var orderItem = new com.example.infrastructure.web.OrderController.CreateOrderRequest.OrderItemRequest();
            orderItem.setProductName("Test Product");
            orderItem.setProductDescription("Test Description");
            orderItem.setQuantity(1);
            orderItem.setUnitPrice(BigDecimal.valueOf(50.00));
            
            request.setOrderItems(List.of(orderItem));

            // When & Then
            mockMvc.perform(post("/api/orders")
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }
}