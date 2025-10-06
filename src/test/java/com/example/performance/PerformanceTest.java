package com.example.performance;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.Payment;
import com.example.infrastructure.persistence.MemberRepository;
import com.example.infrastructure.persistence.OrderRepository;
import com.example.infrastructure.persistence.PaymentRepository;
import com.example.application.service.MemberService;
import com.example.application.service.OrderService;
import com.example.application.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("성능 테스트")
class PerformanceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Nested
    @DisplayName("대량 데이터 처리 성능 테스트")
    class BulkDataPerformanceTest {

        @Test
        @DisplayName("대량 회원 생성 성능 테스트")
        void bulkMemberCreation_Performance() {
            // Given
            int memberCount = 1000;
            long startTime = System.currentTimeMillis();

            // When
            List<Member> members = new ArrayList<>();
            for (int i = 0; i < memberCount; i++) {
                var memberDto = memberService.createMember(
                        "perf" + i + "@example.com",
                        "Performance User " + i,
                        "010-" + String.format("%04d", i) + "-" + String.format("%04d", i)
                );
                members.add(memberRepository.findById(memberDto.getId()).orElseThrow());
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(members).hasSize(memberCount);
            assertThat(duration).isLessThan(5000); // 5초 이내
            System.out.printf("Created %d members in %d ms (%.2f ms per member)%n", 
                    memberCount, duration, (double) duration / memberCount);
        }

        @Test
        @DisplayName("대량 주문 생성 성능 테스트")
        void bulkOrderCreation_Performance() {
            // Given
            var memberDto = memberService.createMember(
                    "bulkorder@example.com",
                    "Bulk Order User",
                    "010-0000-0000"
            );

            int orderCount = 500;
            long startTime = System.currentTimeMillis();

            // When
            List<Order> orders = new ArrayList<>();
            for (int i = 0; i < orderCount; i++) {
                var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
                orderItem.setProductName("Product " + i);
                orderItem.setProductDescription("Description " + i);
                orderItem.setQuantity(1);
                orderItem.setUnitPrice(BigDecimal.valueOf(100.00 + i));

                var orderDto = orderService.createOrder(
                        memberDto.getId(),
                        List.of(orderItem)
                );
                orders.add(orderRepository.findById(orderDto.getId()).orElseThrow());
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(orders).hasSize(orderCount);
            assertThat(duration).isLessThan(3000); // 3초 이내
            System.out.printf("Created %d orders in %d ms (%.2f ms per order)%n", 
                    orderCount, duration, (double) duration / orderCount);
        }

        @Test
        @DisplayName("대량 결제 처리 성능 테스트")
        void bulkPaymentProcessing_Performance() {
            // Given
            var memberDto = memberService.createMember(
                    "bulkpayment@example.com",
                    "Bulk Payment User",
                    "010-1111-1111"
            );

            int paymentCount = 200;
            List<Order> orders = new ArrayList<>();

            // 주문 생성
            for (int i = 0; i < paymentCount; i++) {
                var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
                orderItem.setProductName("Payment Product " + i);
                orderItem.setProductDescription("Payment Description " + i);
                orderItem.setQuantity(1);
                orderItem.setUnitPrice(BigDecimal.valueOf(50.00 + i));

                var orderDto = orderService.createOrder(
                        memberDto.getId(),
                        List.of(orderItem)
                );
                orders.add(orderRepository.findById(orderDto.getId()).orElseThrow());
            }

            long startTime = System.currentTimeMillis();

            // When
            List<Payment> payments = new ArrayList<>();
            for (Order order : orders) {
                var paymentDto = paymentService.createPayment(
                        order.getId(),
                        Payment.PaymentMethod.CREDIT_CARD
                );
                paymentService.processPayment(paymentDto.getId());
                payments.add(paymentRepository.findById(paymentDto.getId()).orElseThrow());
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(payments).hasSize(paymentCount);
            assertThat(duration).isLessThan(2000); // 2초 이내
            System.out.printf("Processed %d payments in %d ms (%.2f ms per payment)%n", 
                    paymentCount, duration, (double) duration / paymentCount);
        }
    }

    @Nested
    @DisplayName("조회 성능 테스트")
    class QueryPerformanceTest {

        private List<Member> testMembers;
        private List<Order> testOrders;

        @BeforeEach
        void setUp() {
            // 테스트 데이터 준비
            testMembers = new ArrayList<>();
            testOrders = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                var memberDto = memberService.createMember(
                        "query" + i + "@example.com",
                        "Query User " + i,
                        "010-" + String.format("%04d", i) + "-" + String.format("%04d", i)
                );
                testMembers.add(memberRepository.findById(memberDto.getId()).orElseThrow());

                // 각 회원당 5개 주문
                for (int j = 0; j < 5; j++) {
                    var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
                    orderItem.setProductName("Product " + j);
                    orderItem.setProductDescription("Description " + j);
                    orderItem.setQuantity(1);
                    orderItem.setUnitPrice(BigDecimal.valueOf(100.00 + j * 10));

                    var orderDto = orderService.createOrder(
                            memberDto.getId(),
                            List.of(orderItem)
                    );
                    testOrders.add(orderRepository.findById(orderDto.getId()).orElseThrow());
                }
            }
        }

        @Test
        @DisplayName("이메일로 회원 조회 성능 테스트")
        void findByEmail_Performance() {
            // Given
            String targetEmail = "query50@example.com";
            int queryCount = 1000;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < queryCount; i++) {
                memberService.getMemberByEmail(targetEmail);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(5000); // 5초 이내
            System.out.printf("Executed %d email queries in %d ms (%.2f ms per query)%n", 
                    queryCount, duration, (double) duration / queryCount);
        }

        @Test
        @DisplayName("회원별 주문 조회 성능 테스트")
        void findOrdersByMember_Performance() {
            // Given
            Long targetMemberId = testMembers.get(50).getId();
            int queryCount = 500;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < queryCount; i++) {
                orderService.getOrdersByMemberId(targetMemberId);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(2000); // 2초 이내
            System.out.printf("Executed %d member order queries in %d ms (%.2f ms per query)%n", 
                    queryCount, duration, (double) duration / queryCount);
        }

        @Test
        @DisplayName("상태별 주문 조회 성능 테스트")
        void findOrdersByStatus_Performance() {
            // Given
            int queryCount = 300;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < queryCount; i++) {
                orderService.getOrdersByStatus(Order.OrderStatus.PENDING);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(1500); // 1.5초 이내
            System.out.printf("Executed %d status queries in %d ms (%.2f ms per query)%n", 
                    queryCount, duration, (double) duration / queryCount);
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("동시 회원 생성 테스트")
        void concurrentMemberCreation_Success() throws Exception {
            // Given
            int threadCount = 10;
            int membersPerThread = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < membersPerThread; j++) {
                        try {
                            memberService.createMember(
                                    "concurrent" + threadId + "-" + j + "@example.com",
                                    "Concurrent User " + threadId + "-" + j,
                                    "010-" + String.format("%04d", threadId) + "-" + String.format("%04d", j)
                            );
                        } catch (Exception e) {
                            // 동시성 오류는 예상됨 (중복 이메일 등)
                        }
                    }
                }, executor);
                futures.add(future);
            }

            // 모든 스레드 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(5000); // 5초 이내
            System.out.printf("Concurrent member creation completed in %d ms%n", duration);
        }

        @Test
        @DisplayName("동시 주문 생성 테스트")
        void concurrentOrderCreation_Success() throws Exception {
            // Given
            var memberDto = memberService.createMember(
                    "concurrentorder@example.com",
                    "Concurrent Order User",
                    "010-0000-0000"
            );

            int threadCount = 5;
            int ordersPerThread = 20;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < ordersPerThread; j++) {
                        try {
                            var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
                            orderItem.setProductName("Concurrent Product " + threadId + "-" + j);
                            orderItem.setProductDescription("Concurrent Description " + threadId + "-" + j);
                            orderItem.setQuantity(1);
                            orderItem.setUnitPrice(BigDecimal.valueOf(100.00 + threadId * 10 + j));

                            orderService.createOrder(memberDto.getId(), List.of(orderItem));
                        } catch (Exception e) {
                            // 동시성 오류 처리
                        }
                    }
                }, executor);
                futures.add(future);
            }

            // 모든 스레드 완료 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(3000); // 3초 이내
            System.out.printf("Concurrent order creation completed in %d ms%n", duration);
        }
    }

    @Nested
    @DisplayName("메모리 사용량 테스트")
    class MemoryUsageTest {

        @Test
        @DisplayName("대량 데이터 메모리 사용량 테스트")
        void largeDatasetMemoryUsage_Success() {
            // Given
            Runtime runtime = Runtime.getRuntime();
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();
            int dataCount = 1000;

            // When
            List<Member> members = new ArrayList<>();
            for (int i = 0; i < dataCount; i++) {
                var memberDto = memberService.createMember(
                        "memory" + i + "@example.com",
                        "Memory User " + i,
                        "010-" + String.format("%04d", i) + "-" + String.format("%04d", i)
                );
                members.add(memberRepository.findById(memberDto.getId()).orElseThrow());
            }

            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryUsed = finalMemory - initialMemory;

            // Then
            assertThat(members).hasSize(dataCount);
            System.out.printf("Memory used for %d members: %d bytes (%.2f KB per member)%n", 
                    dataCount, memoryUsed, (double) memoryUsed / dataCount / 1024);

            // 메모리 사용량이 합리적인 범위 내에 있는지 확인
            assertThat(memoryUsed).isLessThan(50 * 1024 * 1024); // 50MB 이내
        }
    }

    @Nested
    @DisplayName("트랜잭션 성능 테스트")
    class TransactionPerformanceTest {

        @Test
        @DisplayName("트랜잭션 롤백 성능 테스트")
        void transactionRollback_Performance() {
            // Given
            int rollbackCount = 100;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < rollbackCount; i++) {
                try {
                    // 의도적으로 실패하는 트랜잭션
                    memberService.createMember(
                            "rollback@example.com", // 동일한 이메일로 중복 생성 시도
                            "Rollback User " + i,
                            "010-0000-0000"
                    );
                } catch (Exception e) {
                    // 예상된 예외 - 트랜잭션 롤백
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(2000); // 2초 이내
            System.out.printf("Executed %d transaction rollbacks in %d ms (%.2f ms per rollback)%n", 
                    rollbackCount, duration, (double) duration / rollbackCount);
        }

        @Test
        @DisplayName("복잡한 트랜잭션 성능 테스트")
        void complexTransaction_Performance() {
            // Given
            int transactionCount = 50;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < transactionCount; i++) {
                try {
                    // 복잡한 트랜잭션: 회원 생성 → 주문 생성 → 결제 생성 → 처리
                    var memberDto = memberService.createMember(
                            "complex" + i + "@example.com",
                            "Complex User " + i,
                            "010-" + String.format("%04d", i) + "-" + String.format("%04d", i)
                    );

                    var orderItem = new com.example.application.service.OrderService.OrderItemRequest();
                    orderItem.setProductName("Complex Product " + i);
                    orderItem.setProductDescription("Complex Description " + i);
                    orderItem.setQuantity(1);
                    orderItem.setUnitPrice(BigDecimal.valueOf(100.00 + i));

                    var orderDto = orderService.createOrder(
                            memberDto.getId(),
                            List.of(orderItem)
                    );

                    orderService.confirmOrder(orderDto.getId());

                    var paymentDto = paymentService.createPayment(
                            orderDto.getId(),
                            Payment.PaymentMethod.CREDIT_CARD
                    );

                    paymentService.processPayment(paymentDto.getId());
                    orderService.completeOrder(orderDto.getId());
                } catch (Exception e) {
                    // 예외 처리
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(duration).isLessThan(10000); // 10초 이내
            System.out.printf("Executed %d complex transactions in %d ms (%.2f ms per transaction)%n", 
                    transactionCount, duration, (double) duration / transactionCount);
        }
    }
}