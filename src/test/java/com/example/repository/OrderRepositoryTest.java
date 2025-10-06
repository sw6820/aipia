package com.example.repository;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.infrastructure.persistence.MemberRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("OrderRepository 테스트")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        testMember = entityManager.persistAndFlush(testMember);

        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();
        testOrder = entityManager.persistAndFlush(testOrder);
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("주문 저장 성공")
        void save_Success() {
            // Given
            Order newOrder = Order.builder()
                    .orderNumber("ORD-002")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();

            // When
            Order savedOrder = orderRepository.save(newOrder);

            // Then
            assertThat(savedOrder.getId()).isNotNull();
            assertThat(savedOrder.getOrderNumber()).isEqualTo("ORD-002");
            assertThat(savedOrder.getMember()).isEqualTo(testMember);
            assertThat(savedOrder.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200.00));
            assertThat(savedOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        }

        @Test
        @DisplayName("주문 조회 성공")
        void findById_Success() {
            // When
            Optional<Order> foundOrder = orderRepository.findById(testOrder.getId());

            // Then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD-001");
            assertThat(foundOrder.get().getMember()).isEqualTo(testMember);
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회")
        void findById_NotFound() {
            // When
            Optional<Order> foundOrder = orderRepository.findById(999L);

            // Then
            assertThat(foundOrder).isEmpty();
        }

        @Test
        @DisplayName("모든 주문 조회")
        void findAll_Success() {
            // Given
            Order order2 = Order.builder()
                    .orderNumber("ORD-002")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            entityManager.persistAndFlush(order2);

            // When
            List<Order> allOrders = orderRepository.findAll();

            // Then
            assertThat(allOrders).hasSize(2);
            assertThat(allOrders).extracting(Order::getOrderNumber)
                    .containsExactlyInAnyOrder("ORD-001", "ORD-002");
        }

        @Test
        @DisplayName("주문 삭제 성공")
        void delete_Success() {
            // When
            orderRepository.delete(testOrder);
            entityManager.flush();

            // Then
            Optional<Order> deletedOrder = orderRepository.findById(testOrder.getId());
            assertThat(deletedOrder).isEmpty();
        }
    }

    @Nested
    @DisplayName("회원별 주문 조회 테스트")
    class MemberBasedQueryTest {

        @BeforeEach
        void setUp() {
            Member member2 = Member.builder()
                    .email("test2@example.com")
                    .name("Test User 2")
                    .phoneNumber("010-2222-2222")
                    .build();
            member2 = entityManager.persistAndFlush(member2);

            Order order2 = Order.builder()
                    .orderNumber("ORD-002")
                    .member(member2)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            entityManager.persistAndFlush(order2);

            Order order3 = Order.builder()
                    .orderNumber("ORD-003")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(300.00))
                    .build();
            entityManager.persistAndFlush(order3);
        }

        @Test
        @DisplayName("특정 회원의 모든 주문 조회")
        void findByMemberId_Success() {
            // When
            List<Order> memberOrders = orderRepository.findByMemberId(testMember.getId());

            // Then
            assertThat(memberOrders).hasSize(2);
            assertThat(memberOrders).extracting(Order::getOrderNumber)
                    .containsExactlyInAnyOrder("ORD-001", "ORD-003");
            assertThat(memberOrders).extracting(Order::getMember)
                    .containsOnly(testMember);
        }

        @Test
        @DisplayName("존재하지 않는 회원의 주문 조회")
        void findByMemberId_NotFound() {
            // When
            List<Order> memberOrders = orderRepository.findByMemberId(999L);

            // Then
            assertThat(memberOrders).isEmpty();
        }

        @Test
        @DisplayName("주문이 없는 회원의 주문 조회")
        void findByMemberId_NoOrders() {
            // Given
            Member memberWithoutOrders = Member.builder()
                    .email("noorders@example.com")
                    .name("No Orders User")
                    .phoneNumber("010-9999-9999")
                    .build();
            memberWithoutOrders = entityManager.persistAndFlush(memberWithoutOrders);

            // When
            List<Order> memberOrders = orderRepository.findByMemberId(memberWithoutOrders.getId());

            // Then
            assertThat(memberOrders).isEmpty();
        }
    }

    @Nested
    @DisplayName("주문번호 기반 조회 테스트")
    class OrderNumberBasedQueryTest {

        @Test
        @DisplayName("주문번호로 주문 조회 성공")
        void findByOrderNumber_Success() {
            // When
            Optional<Order> foundOrder = orderRepository.findByOrderNumber("ORD-001");

            // Then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD-001");
            assertThat(foundOrder.get().getMember()).isEqualTo(testMember);
        }

        @Test
        @DisplayName("존재하지 않는 주문번호로 조회")
        void findByOrderNumber_NotFound() {
            // When
            Optional<Order> foundOrder = orderRepository.findByOrderNumber("ORD-NONEXISTENT");

            // Then
            assertThat(foundOrder).isEmpty();
        }

        @Test
        @DisplayName("빈 주문번호로 조회")
        void findByOrderNumber_EmptyString() {
            // When
            Optional<Order> foundOrder = orderRepository.findByOrderNumber("");

            // Then
            assertThat(foundOrder).isEmpty();
        }
    }

    @Nested
    @DisplayName("상태 기반 조회 테스트")
    class StatusBasedQueryTest {

        @BeforeEach
        void setUp() {
            Order pendingOrder = Order.builder()
                    .orderNumber("ORD-PENDING")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();
            entityManager.persistAndFlush(pendingOrder);

            Order confirmedOrder = Order.builder()
                    .orderNumber("ORD-CONFIRMED")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            confirmedOrder.confirm();
            entityManager.persistAndFlush(confirmedOrder);

            Order cancelledOrder = Order.builder()
                    .orderNumber("ORD-CANCELLED")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(300.00))
                    .build();
            cancelledOrder.cancel();
            entityManager.persistAndFlush(cancelledOrder);

            Order completedOrder = Order.builder()
                    .orderNumber("ORD-COMPLETED")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(400.00))
                    .build();
            completedOrder.confirm();
            completedOrder.complete();
            entityManager.persistAndFlush(completedOrder);
        }

        @Test
        @DisplayName("대기 상태 주문 조회")
        void findByStatus_Pending() {
            // When
            List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING);

            // Then
            assertThat(pendingOrders).hasSize(2); // ORD-001, ORD-PENDING
            assertThat(pendingOrders).extracting(Order::getStatus)
                    .containsOnly(Order.OrderStatus.PENDING);
        }

        @Test
        @DisplayName("확인된 주문 조회")
        void findByStatus_Confirmed() {
            // When
            List<Order> confirmedOrders = orderRepository.findByStatus(Order.OrderStatus.CONFIRMED);

            // Then
            assertThat(confirmedOrders).hasSize(1);
            assertThat(confirmedOrders).extracting(Order::getStatus)
                    .containsOnly(Order.OrderStatus.CONFIRMED);
            assertThat(confirmedOrders.get(0).getOrderNumber()).isEqualTo("ORD-CONFIRMED");
        }

        @Test
        @DisplayName("취소된 주문 조회")
        void findByStatus_Cancelled() {
            // When
            List<Order> cancelledOrders = orderRepository.findByStatus(Order.OrderStatus.CANCELLED);

            // Then
            assertThat(cancelledOrders).hasSize(1);
            assertThat(cancelledOrders).extracting(Order::getStatus)
                    .containsOnly(Order.OrderStatus.CANCELLED);
            assertThat(cancelledOrders.get(0).getOrderNumber()).isEqualTo("ORD-CANCELLED");
        }

        @Test
        @DisplayName("완료된 주문 조회")
        void findByStatus_Completed() {
            // When
            List<Order> completedOrders = orderRepository.findByStatus(Order.OrderStatus.COMPLETED);

            // Then
            assertThat(completedOrders).hasSize(1);
            assertThat(completedOrders).extracting(Order::getStatus)
                    .containsOnly(Order.OrderStatus.COMPLETED);
            assertThat(completedOrders.get(0).getOrderNumber()).isEqualTo("ORD-COMPLETED");
        }
    }

    @Nested
    @DisplayName("복합 조건 조회 테스트")
    class ComplexQueryTest {

        @BeforeEach
        void setUp() {
            Member member2 = Member.builder()
                    .email("test2@example.com")
                    .name("Test User 2")
                    .phoneNumber("010-2222-2222")
                    .build();
            member2 = entityManager.persistAndFlush(member2);

            Order order2 = Order.builder()
                    .orderNumber("ORD-002")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();
            order2.confirm();
            entityManager.persistAndFlush(order2);

            Order order3 = Order.builder()
                    .orderNumber("ORD-003")
                    .member(member2)
                    .totalAmount(BigDecimal.valueOf(300.00))
                    .build();
            order3.confirm();
            entityManager.persistAndFlush(order3);
        }

        @Test
        @DisplayName("특정 회원의 특정 상태 주문 조회")
        void findByMemberIdAndStatus_Success() {
            // When
            List<Order> confirmedOrders = orderRepository.findByMemberIdAndStatus(
                    testMember.getId(), Order.OrderStatus.CONFIRMED);

            // Then
            assertThat(confirmedOrders).hasSize(1);
            assertThat(confirmedOrders.get(0).getOrderNumber()).isEqualTo("ORD-002");
            assertThat(confirmedOrders.get(0).getMember()).isEqualTo(testMember);
            assertThat(confirmedOrders.get(0).getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("존재하지 않는 회원의 특정 상태 주문 조회")
        void findByMemberIdAndStatus_NotFound() {
            // When
            List<Order> orders = orderRepository.findByMemberIdAndStatus(
                    999L, Order.OrderStatus.CONFIRMED);

            // Then
            assertThat(orders).isEmpty();
        }

        @Test
        @DisplayName("특정 회원의 존재하지 않는 상태 주문 조회")
        void findByMemberIdAndStatus_NoMatchingStatus() {
            // When
            List<Order> cancelledOrders = orderRepository.findByMemberIdAndStatus(
                    testMember.getId(), Order.OrderStatus.CANCELLED);

            // Then
            assertThat(cancelledOrders).isEmpty();
        }
    }

    @Nested
    @DisplayName("연관 관계 조회 테스트")
    class RelationshipQueryTest {

        @Test
        @DisplayName("주문 아이템과 결제 정보와 함께 주문 조회")
        void findByIdWithOrderItemsAndPayment_Success() {
            // Given
            Order orderWithItems = Order.builder()
                    .orderNumber("ORD-WITH-ITEMS")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(150.00))
                    .build();
            orderWithItems = entityManager.persistAndFlush(orderWithItems);

            // When
            Optional<Order> foundOrder = orderRepository.findByIdWithOrderItemsAndPayment(orderWithItems.getId());

            // Then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD-WITH-ITEMS");
            assertThat(foundOrder.get().getOrderItems()).isNotNull();
        }

        @Test
        @DisplayName("회원 정보와 함께 주문 조회")
        void findByIdWithMember_Success() {
            // When
            Optional<Order> foundOrder = orderRepository.findByIdWithMember(testOrder.getId());

            // Then
            assertThat(foundOrder).isPresent();
            assertThat(foundOrder.get().getOrderNumber()).isEqualTo("ORD-001");
            assertThat(foundOrder.get().getMember()).isNotNull();
            assertThat(foundOrder.get().getMember().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 주문의 연관 정보 조회")
        void findByIdWithOrderItemsAndPayment_NotFound() {
            // When
            Optional<Order> foundOrder = orderRepository.findByIdWithOrderItemsAndPayment(999L);

            // Then
            assertThat(foundOrder).isEmpty();
        }
    }

    @Nested
    @DisplayName("데이터 무결성 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("중복 주문번호 저장 시 예외 발생")
        void save_DuplicateOrderNumber_ThrowsException() {
            // Given
            Order duplicateOrder = Order.builder()
                    .orderNumber("ORD-001") // 이미 존재하는 주문번호
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                orderRepository.save(duplicateOrder);
                entityManager.flush();
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("null 주문번호 저장 시 예외 발생")
        void save_NullOrderNumber_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> {
                Order orderWithNullNumber = Order.builder()
                        .orderNumber(null)
                        .member(testMember)
                        .totalAmount(BigDecimal.valueOf(100.00))
                        .build();
            }).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 회원으로 주문 저장 시 예외 발생")
        void save_NullMember_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> {
                Order orderWithNullMember = Order.builder()
                        .orderNumber("ORD-NULL-MEMBER")
                        .member(null)
                        .totalAmount(BigDecimal.valueOf(100.00))
                        .build();
            }).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 총액으로 주문 저장 시 예외 발생")
        void save_NullTotalAmount_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> {
                Order orderWithNullAmount = Order.builder()
                        .orderNumber("ORD-NULL-AMOUNT")
                        .member(testMember)
                        .totalAmount(null)
                        .build();
            }).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("대량 주문 데이터 조회 성능 테스트")
        void findByMemberId_LargeDataset_Performance() {
            // Given
            Member memberWithManyOrders = Member.builder()
                    .email("manyorders@example.com")
                    .name("Many Orders User")
                    .phoneNumber("010-9999-9999")
                    .build();
            memberWithManyOrders = entityManager.persistAndFlush(memberWithManyOrders);

            for (int i = 0; i < 100; i++) {
                Order order = Order.builder()
                        .orderNumber("ORD-PERF-" + i)
                        .member(memberWithManyOrders)
                        .totalAmount(BigDecimal.valueOf(100.00 + i))
                        .build();
                entityManager.persist(order);
            }
            entityManager.flush();

            // When
            long startTime = System.currentTimeMillis();
            List<Order> memberOrders = orderRepository.findByMemberId(memberWithManyOrders.getId());
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(memberOrders).hasSize(100);
            assertThat(endTime - startTime).isLessThan(500); // 500ms 이내
        }

        @Test
        @DisplayName("인덱스 활용 주문번호 조회 성능 테스트")
        void findByOrderNumber_IndexPerformance() {
            // Given
            for (int i = 0; i < 1000; i++) {
                Order order = Order.builder()
                        .orderNumber("ORD-INDEX-" + i)
                        .member(testMember)
                        .totalAmount(BigDecimal.valueOf(100.00 + i))
                        .build();
                entityManager.persist(order);
            }
            entityManager.flush();

            // When
            long startTime = System.currentTimeMillis();
            Optional<Order> foundOrder = orderRepository.findByOrderNumber("ORD-INDEX-500");
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(foundOrder).isPresent();
            assertThat(endTime - startTime).isLessThan(50); // 50ms 이내
        }
    }
}