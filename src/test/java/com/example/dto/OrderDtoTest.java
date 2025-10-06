package com.example.dto;

import com.example.domain.Member;
import com.example.domain.Order;
import com.example.domain.OrderItem;
import com.example.domain.Order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderDto 테스트")
class OrderDtoTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("Builder를 통한 OrderDto 생성")
        void createOrderDto_WithBuilder_Success() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            List<OrderItemDto> orderItems = List.of(
                    OrderItemDto.builder()
                            .productName("Test Product")
                            .quantity(1)
                            .unitPrice(BigDecimal.valueOf(10.00))
                            .build()
            );

            // When
            OrderDto dto = OrderDto.builder()
                    .id(1L)
                    .orderNumber("ORD-001")
                    .memberId(100L)
                    .memberName("John Doe")
                    .totalAmount(BigDecimal.valueOf(50.00))
                    .status(OrderStatus.PENDING)
                    .createdAt(now)
                    .updatedAt(now)
                    .orderItems(orderItems)
                    .build();

            // Then
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getOrderNumber()).isEqualTo("ORD-001");
            assertThat(dto.getMemberId()).isEqualTo(100L);
            assertThat(dto.getMemberName()).isEqualTo("John Doe");
            assertThat(dto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(50.00));
            assertThat(dto.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(dto.getCreatedAt()).isEqualTo(now);
            assertThat(dto.getUpdatedAt()).isEqualTo(now);
            assertThat(dto.getOrderItems()).isEqualTo(orderItems);
        }

        @Test
        @DisplayName("기본 생성자를 통한 OrderDto 생성")
        void createOrderDto_WithDefaultConstructor_Success() {
            // When
            OrderDto dto = OrderDto.builder().build();

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isNull();
            assertThat(dto.getOrderNumber()).isNull();
            assertThat(dto.getMemberId()).isNull();
            assertThat(dto.getMemberName()).isNull();
            assertThat(dto.getTotalAmount()).isNull();
            assertThat(dto.getStatus()).isNull();
            assertThat(dto.getCreatedAt()).isNull();
            assertThat(dto.getUpdatedAt()).isNull();
            assertThat(dto.getOrderItems()).isNull();
            assertThat(dto.getPayment()).isNull();
        }
    }

    @Nested
    @DisplayName("Entity 변환 테스트")
    class EntityConversionTest {

        @Test
        @DisplayName("Order 엔티티에서 OrderDto로 변환")
        void fromOrder_WithValidOrder_Success() {
            // Given
            Member testMember = Member.builder()
                    .email("test@example.com")
                    .name("Test User")
                    .phoneNumber("010-1234-5678")
                    .build();

            Order testOrder = Order.builder()
                    .orderNumber("ORD-TEST")
                    .member(testMember)
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .productName("Test Product")
                    .productDescription("Test Product Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(50.00))
                    .build();

            testOrder.addOrderItem(orderItem);

            // When
            OrderDto result = OrderDto.from(testOrder);

            // Then
            assertThat(result.getId()).isEqualTo(testOrder.getId());
            assertThat(result.getOrderNumber()).isEqualTo(testOrder.getOrderNumber());
            assertThat(result.getMemberId()).isEqualTo(testMember.getId());
            assertThat(result.getMemberName()).isEqualTo(testMember.getName());
            assertThat(result.getTotalAmount()).isEqualTo(testOrder.getTotalAmount());
            assertThat(result.getStatus()).isEqualTo(testOrder.getStatus());
            assertThat(result.getOrderItems()).hasSize(1);
            assertThat(result.getOrderItems().get(0).getProductName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("Member가 null인 Order 생성 시 예외 발생")
        void fromOrder_WithNullMember_ThrowsException() {
            // Given & When & Then
            assertThatThrownBy(() -> Order.builder()
                    .orderNumber("ORD-NULL")
                    .member(null)
                    .totalAmount(BigDecimal.valueOf(50.00))
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Member cannot be null");
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 필드의 Getter 정상 동작")
        void getter_AllFields_Success() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .id(100L)
                    .orderNumber("ORD-GETTER")
                    .memberId(200L)
                    .memberName("Getter User")
                    .totalAmount(BigDecimal.valueOf(150.00))
                    .status(OrderStatus.COMPLETED)
                    .build();

            // When & Then
            assertThat(dto.getId()).isEqualTo(100L);
            assertThat(dto.getOrderNumber()).isEqualTo("ORD-GETTER");
            assertThat(dto.getMemberId()).isEqualTo(200L);
            assertThat(dto.getMemberName()).isEqualTo("Getter User");
            assertThat(dto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(150.00));
            assertThat(dto.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("null 값들에 대한 Getter 정상 동작")
        void getter_NullValues_Success() {
            // Given
            OrderDto dto = OrderDto.builder().build();

            // When & Then
            assertThat(dto.getId()).isNull();
            assertThat(dto.getOrderNumber()).isNull();
            assertThat(dto.getMemberId()).isNull();
            assertThat(dto.getMemberName()).isNull();
            assertThat(dto.getTotalAmount()).isNull();
            assertThat(dto.getStatus()).isNull();
            assertThat(dto.getOrderItems()).isNull();
            assertThat(dto.getPayment()).isNull();
        }
    }

    @Nested
    @DisplayName("Equals/HashCode 테스트")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("동일한 필드를 가진 OrderDto는 equals true")
        void equals_SameFields_True() {
            // Given
            OrderDto dto1 = OrderDto.builder()
                    .id(1L)
                    .orderNumber("ORD-EQUAL")
                    .memberId(100L)
                    .memberName("Equal User")
                    .totalAmount(BigDecimal.valueOf(50.00))
                    .status(OrderStatus.PENDING)
                    .build();

            OrderDto dto2 = OrderDto.builder()
                    .id(1L)
                    .orderNumber("ORD-EQUAL")
                    .memberId(100L)
                    .memberName("Equal User")
                    .totalAmount(BigDecimal.valueOf(50.00))
                    .status(OrderStatus.PENDING)
                    .build();

            // When & Then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 OrderDto는 equals false")
        void equals_DifferentFields_False() {
            // Given
            OrderDto dto1 = OrderDto.builder()
                    .id(1L)
                    .orderNumber("ORD-DIFF1")
                    .memberId(100L)
                    .memberName("Different User 1")
                    .totalAmount(BigDecimal.valueOf(50.00))
                    .status(OrderStatus.PENDING)
                    .build();

            OrderDto dto2 = OrderDto.builder()
                    .id(2L)
                    .orderNumber("ORD-DIFF2")
                    .memberId(200L)
                    .memberName("Different User 2")
                    .totalAmount(BigDecimal.valueOf(100.00))
                    .status(OrderStatus.COMPLETED)
                    .build();

            // When & Then
            assertThat(dto1).isNotEqualTo(dto2);
        }

        @Test
        @DisplayName("null과의 equals 비교")
        void equals_Null_False() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .id(1L)
                    .orderNumber("ORD-NULL")
                    .build();

            // When & Then
            assertThat(dto).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과의 equals 비교")
        void equals_DifferentType_False() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .id(1L)
                    .orderNumber("ORD-TYPE")
                    .build();

            // When & Then
            assertThat(dto).isNotEqualTo("Not an OrderDto");
        }

        @Test
        @DisplayName("자기 자신과의 equals 비교")
        void equals_Self_True() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .id(1L)
                    .orderNumber("ORD-SELF")
                    .build();

            // When & Then
            assertThat(dto).isEqualTo(dto);
        }
    }

    @Nested
    @DisplayName("ToString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("ToString 메서드 정상 동작")
        void toString_ValidDto_Success() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .id(1L)
                    .orderNumber("ORD-TOSTRING")
                    .memberId(100L)
                    .memberName("ToString User")
                    .totalAmount(BigDecimal.valueOf(75.50))
                    .status(OrderStatus.CONFIRMED)
                    .build();

            // When
            String result = dto.toString();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("OrderDto");
            assertThat(result).contains("ORD-TOSTRING");
            assertThat(result).contains("ToString User");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("주문 번호가 null이거나 빈 문자열인 경우")
        void validation_InvalidOrderNumber(String orderNumber) {
            // Given
            OrderDto dto = OrderDto.builder()
                    .orderNumber(orderNumber)
                    .totalAmount(BigDecimal.valueOf(50.00))
                    .build();

            // When & Then
            // Note: OrderDto doesn't have validation logic, this is just for testing getter
            assertThat(dto.getOrderNumber()).isEqualTo(orderNumber);
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.0, -0.01})
        @DisplayName("총 금액이 음수인 경우")
        void validation_NegativeTotalAmount(double amount) {
            // Given
            OrderDto dto = OrderDto.builder()
                    .orderNumber("ORD-NEGATIVE")
                    .totalAmount(BigDecimal.valueOf(amount))
                    .build();

            // When & Then
            // Note: OrderDto doesn't have validation logic, this is just for testing getter
            assertThat(dto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(amount));
        }
    }

    @Nested
    @DisplayName("상태 관련 테스트")
    class StatusTest {

        @Test
        @DisplayName("PENDING 상태 확인")
        void status_Pending_Success() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .status(OrderStatus.PENDING)
                    .build();

            // When & Then
            assertThat(dto.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("CONFIRMED 상태 확인")
        void status_Confirmed_Success() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .status(OrderStatus.CONFIRMED)
                    .build();

            // When & Then
            assertThat(dto.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }

        @Test
        @DisplayName("COMPLETED 상태 확인")
        void status_Completed_Success() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .status(OrderStatus.COMPLETED)
                    .build();

            // When & Then
            assertThat(dto.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("CANCELLED 상태 확인")
        void status_Cancelled_Success() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .status(OrderStatus.CANCELLED)
                    .build();

            // When & Then
            assertThat(dto.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("주문 아이템 관련 테스트")
    class OrderItemTest {

        @Test
        @DisplayName("주문 아이템 리스트 설정 및 조회")
        void orderItems_SetAndGet_Success() {
            // Given
            List<OrderItemDto> orderItems = List.of(
                    OrderItemDto.builder()
                            .productName("Product 1")
                            .quantity(1)
                            .unitPrice(BigDecimal.valueOf(10.00))
                            .build(),
                    OrderItemDto.builder()
                            .productName("Product 2")
                            .quantity(2)
                            .unitPrice(BigDecimal.valueOf(20.00))
                            .build()
            );

            OrderDto dto = OrderDto.builder()
                    .orderItems(orderItems)
                    .build();

            // When & Then
            assertThat(dto.getOrderItems()).isEqualTo(orderItems);
            assertThat(dto.getOrderItems()).hasSize(2);
        }

        @Test
        @DisplayName("빈 주문 아이템 리스트")
        void orderItems_EmptyList_Success() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .orderItems(List.of())
                    .build();

            // When & Then
            assertThat(dto.getOrderItems()).isEmpty();
        }

        @Test
        @DisplayName("null 주문 아이템 리스트")
        void orderItems_NullList_Success() {
            // Given
            OrderDto dto = OrderDto.builder()
                    .orderItems(null)
                    .build();

            // When & Then
            assertThat(dto.getOrderItems()).isNull();
        }
    }

    @Nested
    @DisplayName("시간 관련 테스트")
    class TimeTest {

        @Test
        @DisplayName("생성 시간 설정 및 조회")
        void createdAt_SetAndGet_Success() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            OrderDto dto = OrderDto.builder()
                    .createdAt(now)
                    .build();

            // When & Then
            assertThat(dto.getCreatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("수정 시간 설정 및 조회")
        void updatedAt_SetAndGet_Success() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            OrderDto dto = OrderDto.builder()
                    .updatedAt(now)
                    .build();

            // When & Then
            assertThat(dto.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("생성 시간과 수정 시간이 다른 경우")
        void time_DifferentCreatedAndUpdated_Success() {
            // Given
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 11, 0);

            OrderDto dto = OrderDto.builder()
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            // When & Then
            assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
            assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
            assertThat(dto.getCreatedAt()).isNotEqualTo(dto.getUpdatedAt());
        }
    }
}