package com.example.integration;

import com.example.domain.*;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("엔티티 관계 통합 테스트")
class EntityRelationshipIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Member testMember;
    private Order testOrder;
    private Payment testPayment;
    private OrderItem testOrderItem1;
    private OrderItem testOrderItem2;

    @BeforeEach
    void setUp() {
        // 테스트 멤버 생성
        testMember = Member.builder()
                .name("테스트 사용자")
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .build();
        testMember = entityManager.persistAndFlush(testMember);

        // 테스트 주문 생성
        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(150.00))
                .build();
        testOrder = entityManager.persistAndFlush(testOrder);

        // 테스트 주문 아이템 생성
        testOrderItem1 = OrderItem.builder()
                .productName("상품1")
                .productDescription("상품1 설명")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build();
        testOrderItem1.setOrder(testOrder);
        testOrderItem1 = entityManager.persistAndFlush(testOrderItem1);

        testOrderItem2 = OrderItem.builder()
                .productName("상품2")
                .productDescription("상품2 설명")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build();
        testOrderItem2.setOrder(testOrder);
        testOrderItem2 = entityManager.persistAndFlush(testOrderItem2);

        // 테스트 결제 생성
        testPayment = Payment.builder()
                .order(testOrder)
                .amount(BigDecimal.valueOf(150.00))
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .build();
        testPayment = entityManager.persistAndFlush(testPayment);

        // 엔티티 매니저 초기화
        entityManager.clear();
    }

    @Nested
    @DisplayName("Member ↔ Order 관계 테스트")
    class MemberOrderRelationshipTest {

        @Test
        @DisplayName("멤버에서 주문 조회 - 양방향 관계 확인")
        void findOrdersFromMember_Success() {
            // When
            Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
            List<Order> orders = foundMember.getOrders();

            // Then
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getId()).isEqualTo(testOrder.getId());
            assertThat(orders.get(0).getOrderNumber()).isEqualTo("ORD-001");
            assertThat(orders.get(0).getMember().getId()).isEqualTo(testMember.getId());
        }

        @Test
        @DisplayName("주문에서 멤버 조회 - 양방향 관계 확인")
        void findMemberFromOrder_Success() {
            // When
            Order foundOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            Member member = foundOrder.getMember();

            // Then
            assertThat(member.getId()).isEqualTo(testMember.getId());
            assertThat(member.getName()).isEqualTo("테스트 사용자");
            assertThat(member.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("멤버 삭제 시 연관 주문도 삭제 - Cascade 테스트")
        void deleteMember_CascadesToOrders() {
            // Given
            Long memberId = testMember.getId();
            Long orderId = testOrder.getId();

            // When
            memberRepository.deleteById(memberId);
            entityManager.flush();

            // Then
            assertThat(memberRepository.findById(memberId)).isEmpty();
            assertThat(orderRepository.findById(orderId)).isEmpty();
        }

        @Test
        @DisplayName("한 멤버가 여러 주문을 가질 수 있음")
        void memberCanHaveMultipleOrders() {
            // Given
            Order secondOrder = Order.builder()
                    .orderNumber("ORD-002")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            secondOrder = entityManager.persistAndFlush(secondOrder);

            // When
            Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
            List<Order> orders = foundMember.getOrders();

            // Then
            assertThat(orders).hasSize(2);
            assertThat(orders).extracting(Order::getOrderNumber)
                    .containsExactlyInAnyOrder("ORD-001", "ORD-002");
        }
    }

    @Nested
    @DisplayName("Order ↔ OrderItem 관계 테스트")
    class OrderOrderItemRelationshipTest {

        @Test
        @DisplayName("주문에서 주문 아이템 조회 - 양방향 관계 확인")
        void findOrderItemsFromOrder_Success() {
            // When
            Order foundOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            List<OrderItem> orderItems = foundOrder.getOrderItems();

            // Then
            assertThat(orderItems).hasSize(2);
            assertThat(orderItems).extracting(OrderItem::getProductName)
                    .containsExactlyInAnyOrder("상품1", "상품2");
            assertThat(orderItems).allMatch(item -> item.getOrder().getId().equals(testOrder.getId()));
        }

        @Test
        @DisplayName("주문 아이템에서 주문 조회 - 양방향 관계 확인")
        void findOrderFromOrderItem_Success() {
            // When
            OrderItem foundOrderItem = entityManager.find(OrderItem.class, testOrderItem1.getId());
            Order order = foundOrderItem.getOrder();

            // Then
            assertThat(order.getId()).isEqualTo(testOrder.getId());
            assertThat(order.getOrderNumber()).isEqualTo("ORD-001");
            assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        }

        @Test
        @DisplayName("주문 삭제 시 연관 주문 아이템도 삭제 - Cascade 테스트")
        void deleteOrder_CascadesToOrderItems() {
            // Given
            Long orderId = testOrder.getId();
            Long orderItem1Id = testOrderItem1.getId();
            Long orderItem2Id = testOrderItem2.getId();

            // When
            orderRepository.deleteById(orderId);
            entityManager.flush();

            // Then
            assertThat(orderRepository.findById(orderId)).isEmpty();
            assertThat(entityManager.find(OrderItem.class, orderItem1Id)).isNull();
            assertThat(entityManager.find(OrderItem.class, orderItem2Id)).isNull();
        }

        @Test
        @DisplayName("주문 아이템의 총 가격 계산 검증")
        void orderItemTotalPriceCalculation() {
            // When
            OrderItem foundOrderItem1 = entityManager.find(OrderItem.class, testOrderItem1.getId());
            OrderItem foundOrderItem2 = entityManager.find(OrderItem.class, testOrderItem2.getId());

            // Then
            // 상품1: 2개 × 50원 = 100원
            assertThat(foundOrderItem1.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
            // 상품2: 1개 × 50원 = 50원
            assertThat(foundOrderItem2.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        }
    }

    @Nested
    @DisplayName("Order ↔ Payment 관계 테스트")
    class OrderPaymentRelationshipTest {

        @Test
        @DisplayName("주문에서 결제 조회 - 양방향 관계 확인")
        void findPaymentFromOrder_Success() {
            // When
            Order foundOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            Payment payment = foundOrder.getPayment();

            // Then
            assertThat(payment).isNotNull();
            assertThat(payment.getId()).isEqualTo(testPayment.getId());
            assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
            assertThat(payment.getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        }

        @Test
        @DisplayName("결제에서 주문 조회 - 양방향 관계 확인")
        void findOrderFromPayment_Success() {
            // When
            Payment foundPayment = paymentRepository.findById(testPayment.getId()).orElseThrow();
            Order order = foundPayment.getOrder();

            // Then
            assertThat(order.getId()).isEqualTo(testOrder.getId());
            assertThat(order.getOrderNumber()).isEqualTo("ORD-001");
            assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        }

        @Test
        @DisplayName("주문 삭제 시 연관 결제도 삭제 - Cascade 테스트")
        void deleteOrder_CascadesToPayment() {
            // Given
            Long orderId = testOrder.getId();
            Long paymentId = testPayment.getId();

            // When
            orderRepository.deleteById(orderId);
            entityManager.flush();

            // Then
            assertThat(orderRepository.findById(orderId)).isEmpty();
            assertThat(paymentRepository.findById(paymentId)).isEmpty();
        }

        @Test
        @DisplayName("하나의 주문은 하나의 결제만 가질 수 있음 - OneToOne 제약")
        void orderCanHaveOnlyOnePayment() {
            // Given
            Payment secondPayment = Payment.builder()
                    .order(testOrder)
                    .amount(BigDecimal.valueOf(150.00))
                    .paymentMethod(Payment.PaymentMethod.CASH)
                    .build();

            // When & Then
            // 같은 주문에 대한 두 번째 결제 생성 시도는 ConstraintViolationException 발생
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(secondPayment);
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("복합 관계 테스트 (Member → Order → OrderItem/Payment)")
    class ComplexRelationshipTest {

        @Test
        @DisplayName("멤버에서 주문 아이템까지 조회 - 3단계 관계")
        void findOrderItemsThroughMember_Success() {
            // When
            Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
            List<Order> orders = foundMember.getOrders();
            List<OrderItem> allOrderItems = orders.stream()
                    .flatMap(order -> order.getOrderItems().stream())
                    .toList();

            // Then
            assertThat(allOrderItems).hasSize(2);
            assertThat(allOrderItems).extracting(OrderItem::getProductName)
                    .containsExactlyInAnyOrder("상품1", "상품2");
        }

        @Test
        @DisplayName("멤버에서 결제까지 조회 - 3단계 관계")
        void findPaymentsThroughMember_Success() {
            // When
            Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
            List<Order> orders = foundMember.getOrders();
            List<Payment> allPayments = orders.stream()
                    .map(Order::getPayment)
                    .filter(payment -> payment != null)
                    .toList();

            // Then
            assertThat(allPayments).hasSize(1);
            assertThat(allPayments.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
            assertThat(allPayments.get(0).getPaymentMethod()).isEqualTo(Payment.PaymentMethod.CREDIT_CARD);
        }

        @Test
        @DisplayName("멤버의 모든 주문 총액 계산")
        void calculateTotalAmountForMember_Success() {
            // Given
            Order secondOrder = Order.builder()
                    .orderNumber("ORD-002")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            secondOrder = entityManager.persistAndFlush(secondOrder);

            // When
            Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
            BigDecimal totalAmount = foundMember.getOrders().stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Then
            assertThat(totalAmount).isEqualByComparingTo(BigDecimal.valueOf(350.00)); // 150 + 200
        }

        @Test
        @DisplayName("멤버의 모든 결제 상태 확인")
        void checkAllPaymentStatusesForMember_Success() {
            // When
            Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
            List<Payment.PaymentStatus> paymentStatuses = foundMember.getOrders().stream()
                    .map(Order::getPayment)
                    .filter(payment -> payment != null)
                    .map(Payment::getStatus)
                    .toList();

            // Then
            assertThat(paymentStatuses).hasSize(1);
            assertThat(paymentStatuses.get(0)).isEqualTo(Payment.PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("데이터 일관성 테스트")
    class DataConsistencyTest {

        @Test
        @DisplayName("주문 총액과 주문 아이템 총액 합계 일치 확인")
        void orderTotalAmountMatchesOrderItemsSum_Success() {
            // When
            Order foundOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            BigDecimal orderItemsSum = foundOrder.getOrderItems().stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Then
            assertThat(foundOrder.getTotalAmount()).isEqualTo(orderItemsSum);
        }

        @Test
        @DisplayName("결제 금액과 주문 총액 일치 확인")
        void paymentAmountMatchesOrderTotal_Success() {
            // When
            Order foundOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            Payment payment = foundOrder.getPayment();

            // Then
            assertThat(payment.getAmount()).isEqualByComparingTo(foundOrder.getTotalAmount());
        }

        @Test
        @DisplayName("주문 상태와 결제 상태 연관성 확인")
        void orderStatusAndPaymentStatusConsistency_Success() {
            // Given
            Order foundOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            Payment payment = foundOrder.getPayment();

            // When
            foundOrder.confirm();
            entityManager.persistAndFlush(foundOrder);

            // Then
            assertThat(foundOrder.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
            // 결제 상태는 주문 상태와 독립적이므로 변경되지 않음
            assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("멤버 상태 변경 시 연관 엔티티에 영향 없음")
        void memberStatusChangeDoesNotAffectRelatedEntities_Success() {
            // Given
            Member foundMember = memberRepository.findById(testMember.getId()).orElseThrow();
            Order foundOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            Payment foundPayment = paymentRepository.findById(testPayment.getId()).orElseThrow();

            // When
            foundMember.deactivate();
            entityManager.persistAndFlush(foundMember);

            // Then
            assertThat(foundMember.getStatus()).isEqualTo(Member.MemberStatus.INACTIVE);
            // 연관 엔티티들은 영향받지 않음
            assertThat(foundOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
            assertThat(foundPayment.getStatus()).isEqualTo(Payment.PaymentStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("대량 데이터에서 관계 조회 성능 테스트")
        void bulkDataRelationshipQueryPerformance_Success() {
            // Given - 대량 데이터 생성
            Member bulkMember = Member.builder()
                    .name("대량 테스트 사용자")
                    .email("bulk@example.com")
                    .phoneNumber("010-9999-9999")
                    .build();
            bulkMember = entityManager.persistAndFlush(bulkMember);

            // 10개의 주문 생성
            for (int i = 0; i < 10; i++) {
                Order order = Order.builder()
                        .orderNumber("BULK-ORD-" + String.format("%03d", i))
                        .member(bulkMember)
                        .totalAmount(BigDecimal.valueOf(100.00 + i))
                        .build();
                order = entityManager.persistAndFlush(order);

                // 각 주문에 3개의 주문 아이템 생성
                for (int j = 0; j < 3; j++) {
                    OrderItem orderItem = OrderItem.builder()
                            .productName("대량상품-" + i + "-" + j)
                            .productDescription("대량상품 설명")
                            .quantity(j + 1)
                            .unitPrice(BigDecimal.valueOf(10.00))
                            .build();
                    orderItem.setOrder(order);
                    entityManager.persistAndFlush(orderItem);
                }

                // 각 주문에 결제 생성
                Payment payment = Payment.builder()
                        .order(order)
                        .amount(order.getTotalAmount())
                        .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                        .build();
                entityManager.persistAndFlush(payment);
            }

            entityManager.flush();
            entityManager.clear();

            // When - 성능 측정
            long startTime = System.currentTimeMillis();
            
            Member foundMember = memberRepository.findById(bulkMember.getId()).orElseThrow();
            List<Order> orders = foundMember.getOrders();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // Then
            assertThat(orders).hasSize(10);
            assertThat(executionTime).isLessThan(1000); // 1초 이내 완료
            
            // 추가 검증: 모든 주문의 주문 아이템과 결제 확인
            for (Order order : orders) {
                assertThat(order.getOrderItems()).hasSize(3);
                assertThat(order.getPayment()).isNotNull();
            }
        }
    }
}
