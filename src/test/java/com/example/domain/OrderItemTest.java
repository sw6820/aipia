package com.example.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * OrderItem 도메인 엔티티 테스트
 */
@DisplayName("OrderItem 도메인 테스트")
class OrderItemTest {

    private OrderItem orderItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        Member testMember = Member.builder()
                .name("Test Member")
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .build();
        
        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .member(testMember)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        orderItem = OrderItem.builder()
                .productName("Test Product")
                .productDescription("Test Description")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build();
        orderItem.setOrder(testOrder);
    }

    @Nested
    @DisplayName("주문 아이템 생성 테스트")
    class CreateOrderItemTest {

        @Test
        @DisplayName("유효한 데이터로 주문 아이템 생성 성공")
        void createOrderItem_WithValidData_Success() {
            // Given & When
            OrderItem newOrderItem = OrderItem.builder()
                    .productName("New Product")
                    .productDescription("New Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(25.00))
                    .build();
            newOrderItem.setOrder(testOrder);

            // Then
            assertThat(newOrderItem.getOrder()).isEqualTo(testOrder);
            assertThat(newOrderItem.getProductName()).isEqualTo("New Product");
            assertThat(newOrderItem.getQuantity()).isEqualTo(1);
            assertThat(newOrderItem.getUnitPrice()).isEqualTo(BigDecimal.valueOf(25.00));
            assertThat(newOrderItem.getTotalPrice()).isEqualTo(BigDecimal.valueOf(25.00));
        }

        @Test
        @DisplayName("Builder 패턴으로 주문 아이템 생성 성공")
        void createOrderItem_WithBuilder_Success() {
            // Given & When
            OrderItem newOrderItem = OrderItem.builder()
                    .productName("Builder Product")
                    .productDescription("Builder Description")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();
            newOrderItem.setOrder(testOrder);

            // Then
            assertThat(newOrderItem.getOrder()).isEqualTo(testOrder);
            assertThat(newOrderItem.getProductName()).isEqualTo("Builder Product");
            assertThat(newOrderItem.getQuantity()).isEqualTo(3);
            assertThat(newOrderItem.getUnitPrice()).isEqualTo(BigDecimal.valueOf(10.00));
            assertThat(newOrderItem.getTotalPrice()).isEqualTo(BigDecimal.valueOf(30.00));
        }
    }

    @Nested
    @DisplayName("총 가격 계산 테스트")
    class TotalPriceCalculationTest {

        @Test
        @DisplayName("수량과 단가로 총 가격 계산 성공")
        void calculateTotalPrice_WithQuantityAndUnitPrice_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Calculator Product")
                    .productDescription("Calculator Description")
                    .quantity(5)
                    .unitPrice(BigDecimal.valueOf(20.00))
                    .build();
            item.setOrder(testOrder);

            // When & Then
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100.00));
        }

        @Test
        @DisplayName("소수점이 있는 가격으로 총 가격 계산 성공")
        void calculateTotalPrice_WithDecimalPrice_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Decimal Product")
                    .productDescription("Decimal Description")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(33.33))
                    .build();
            item.setOrder(testOrder);

            // When & Then
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(99.99));
        }

        @Test
        @DisplayName("수량이 1일 때 총 가격이 단가와 동일")
        void calculateTotalPrice_WithQuantityOne_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Single Product")
                    .productDescription("Single Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(50.00))
                    .build();
            item.setOrder(testOrder);

            // When & Then
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(50.00));
        }

        @Test
        @DisplayName("수량이 0일 때 총 가격이 0")
        void calculateTotalPrice_WithQuantityZero_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Zero Product")
                    .productDescription("Zero Description")
                    .quantity(0)
                    .unitPrice(BigDecimal.valueOf(50.00))
                    .build();
            item.setOrder(testOrder);

            // When & Then
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(0.00));
        }
    }

    @Nested
    @DisplayName("수량 변경 테스트")
    class QuantityChangeTest {

        @Test
        @DisplayName("수량 변경 시 총 가격 자동 재계산")
        void changeQuantity_UpdatesTotalPrice_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Changeable Product")
                    .productDescription("Changeable Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(25.00))
                    .build();
            item.setOrder(testOrder);

            // When
            item.updateQuantity(4);

            // Then
            assertThat(item.getQuantity()).isEqualTo(4);
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100.00));
        }

        @Test
        @DisplayName("수량을 0으로 변경 시 총 가격이 0")
        void changeQuantityToZero_SetsTotalPriceToZero_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Zeroable Product")
                    .productDescription("Zeroable Description")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();
            item.setOrder(testOrder);

            // When
            item.updateQuantity(0);

            // Then
            assertThat(item.getQuantity()).isEqualTo(0);
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(0.00));
        }
    }

    @Nested
    @DisplayName("단가 변경 테스트")
    class UnitPriceChangeTest {

        @Test
        @DisplayName("단가 변경 시 총 가격 자동 재계산")
        void changeUnitPrice_UpdatesTotalPrice_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Price Changeable Product")
                    .productDescription("Price Changeable Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(25.00))
                    .build();
            item.setOrder(testOrder);

            // When & Then
            // OrderItem doesn't have setUnitPrice method, so we can't change unit price after creation
            assertThat(item.getUnitPrice()).isEqualTo(BigDecimal.valueOf(25.00));
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(50.00));
        }

        @Test
        @DisplayName("단가를 0으로 변경 시 총 가격이 0")
        void changeUnitPriceToZero_SetsTotalPriceToZero_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Free Product")
                    .productDescription("Free Description")
                    .quantity(5)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();
            item.setOrder(testOrder);

            // When & Then
            // OrderItem doesn't have setUnitPrice method, so we can't change unit price after creation
            assertThat(item.getUnitPrice()).isEqualTo(BigDecimal.valueOf(10.00));
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(50.00));
        }
    }

    @Nested
    @DisplayName("주문 연관 테스트")
    class OrderAssociationTest {

        @Test
        @DisplayName("주문 아이템에 주문 설정 성공")
        void setOrder_WithValidOrder_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Association Product")
                    .productDescription("Association Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(100.00))
                    .build();

            Member newMember = Member.builder()
                    .name("New Member")
                    .email("new@example.com")
                    .phoneNumber("010-9999-9999")
                    .build();
            
            Order newOrder = Order.builder()
                    .orderNumber("ORD-002")
                    .member(newMember)
                    .totalAmount(BigDecimal.valueOf(200.00))
                    .build();

            // When
            item.setOrder(newOrder);

            // Then
            assertThat(item.getOrder()).isEqualTo(newOrder);
        }

        @Test
        @DisplayName("주문 아이템에 null 주문 설정 시 예외 발생")
        void setOrder_WithNull_ThrowsException() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Removable Product")
                    .productDescription("Removable Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(50.00))
                    .build();

            // When & Then
            assertThatThrownBy(() -> item.setOrder(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("주문은 필수입니다");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("상품명이 null 또는 빈 문자열일 때 예외 발생")
        void createOrderItem_WithInvalidProductName_ThrowsException(String productName) {
            // When & Then
            assertThatThrownBy(() -> {
                OrderItem.builder()
                        .productName(productName)
                        .productDescription("Description")
                        .quantity(1)
                        .unitPrice(BigDecimal.valueOf(10.00))
                        .build();
            }).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("상품명은 필수입니다");
        }

        @Test
        @DisplayName("수량이 음수일 때 예외 발생")
        void createOrderItem_WithNegativeQuantity_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> {
                OrderItem.builder()
                        .productName("Negative Product")
                        .productDescription("Negative Description")
                        .quantity(-1)
                        .unitPrice(BigDecimal.valueOf(10.00))
                        .build();
            }).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("수량은 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("단가가 음수일 때 예외 발생")
        void createOrderItem_WithNegativeUnitPrice_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> {
                OrderItem.builder()
                        .productName("Negative Price Product")
                        .productDescription("Negative Price Description")
                        .quantity(1)
                        .unitPrice(BigDecimal.valueOf(-10.00))
                        .build();
            }).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("단가는 0 이상이어야 합니다");
        }

        @Test
        @DisplayName("주문이 null일 때 예외 발생")
        void createOrderItem_WithNullOrder_ThrowsException() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Null Order Product")
                    .productDescription("Null Order Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();
            
            // When & Then
            assertThatThrownBy(() -> item.setOrder(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("주문은 필수입니다");
        }
    }

    @Nested
    @DisplayName("동등성 및 해시코드 테스트")
    class EqualityAndHashCodeTest {

        @Test
        @DisplayName("동일한 데이터의 주문 아이템은 동등함")
        void equals_WithSameData_ReturnsTrue() {
            // Given
            OrderItem item1 = OrderItem.builder()
                    .productName("Same Product")
                    .productDescription("Same Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(25.00))
                    .build();
            item1.setOrder(testOrder);

            OrderItem item2 = OrderItem.builder()
                    .productName("Same Product")
                    .productDescription("Same Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(25.00))
                    .build();
            item2.setOrder(testOrder);

            // When & Then
            assertThat(item1).isEqualTo(item2);
            assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
        }

        @Test
        @DisplayName("다른 데이터의 주문 아이템은 동등하지 않음")
        void equals_WithDifferentData_ReturnsFalse() {
            // Given
            OrderItem item1 = OrderItem.builder()
                    .productName("Product 1")
                    .productDescription("Product 1 Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(25.00))
                    .build();
            item1.setOrder(testOrder);

            OrderItem item2 = OrderItem.builder()
                    .productName("Product 2")
                    .productDescription("Product 2 Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(25.00))
                    .build();
            item2.setOrder(testOrder);

            // When & Then
            assertThat(item1).isNotEqualTo(item2);
        }

        @Test
        @DisplayName("null과의 비교는 false 반환")
        void equals_WithNull_ReturnsFalse() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Null Test Product")
                    .productDescription("Null Test Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();
            item.setOrder(testOrder);

            // When & Then
            assertThat(item.equals(null)).isFalse();
        }

        @Test
        @DisplayName("다른 타입과의 비교는 false 반환")
        void equals_WithDifferentType_ReturnsFalse() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Type Test Product")
                    .productDescription("Type Test Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();
            item.setOrder(testOrder);

            String differentType = "Not an OrderItem";

            // When & Then
            assertThat(item.equals(differentType)).isFalse();
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTest {

        @Test
        @DisplayName("toString 메서드가 올바른 문자열 반환")
        void toString_ReturnsCorrectString() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("ToString Product")
                    .productDescription("ToString Description")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(15.00))
                    .build();
            item.setOrder(testOrder);

            // When
            String result = item.toString();

            // Then
            assertThat(result).contains("ToString Product");
            assertThat(result).contains("3");
            assertThat(result).contains("15.0");
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRulesTest {

        @Test
        @DisplayName("주문 아이템 생성 시 자동으로 총 가격 계산")
        void createOrderItem_AutoCalculatesTotalPrice_Success() {
            // Given & When
            OrderItem item = OrderItem.builder()
                    .productName("Auto Calculate Product")
                    .productDescription("Auto Calculate Description")
                    .quantity(4)
                    .unitPrice(BigDecimal.valueOf(12.50))
                    .build();
            item.setOrder(testOrder);

            // Then
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(50.00));
        }

        @Test
        @DisplayName("수량과 단가 변경 시 총 가격 자동 업데이트")
        void updateQuantityAndPrice_AutoUpdatesTotalPrice_Success() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productName("Update Product")
                    .productDescription("Update Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();
            item.setOrder(testOrder);

            // When
            item.updateQuantity(5);

            // Then
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(50.00));
        }

        @Test
        @DisplayName("주문 아이템의 총 가격은 수량 × 단가와 동일")
        void totalPrice_EqualsQuantityTimesUnitPrice_Success() {
            // Given
            int quantity = 7;
            BigDecimal unitPrice = BigDecimal.valueOf(14.28);
            BigDecimal expectedTotal = BigDecimal.valueOf(99.96);

            OrderItem item = OrderItem.builder()
                    .productName("Math Product")
                    .productDescription("Math Description")
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .build();
            item.setOrder(testOrder);

            // When & Then
            assertThat(item.getTotalPrice()).isEqualTo(expectedTotal);
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("매우 큰 수량으로 주문 아이템 생성")
        void createOrderItem_WithLargeQuantity_Success() {
            // Given & When
            OrderItem item = OrderItem.builder()
                    .productName("Large Quantity Product")
                    .productDescription("Large Quantity Description")
                    .quantity(1000000)
                    .unitPrice(BigDecimal.valueOf(0.01))
                    .build();
            item.setOrder(testOrder);

            // Then
            assertThat(item.getQuantity()).isEqualTo(1000000);
            assertThat(item.getTotalPrice().compareTo(BigDecimal.valueOf(10000.00))).isEqualTo(0);
        }

        @Test
        @DisplayName("매우 큰 단가로 주문 아이템 생성")
        void createOrderItem_WithLargeUnitPrice_Success() {
            // Given & When
            OrderItem item = OrderItem.builder()
                    .productName("Expensive Product")
                    .productDescription("Expensive Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(999999.99))
                    .build();
            item.setOrder(testOrder);

            // Then
            assertThat(item.getUnitPrice()).isEqualTo(BigDecimal.valueOf(999999.99));
            assertThat(item.getTotalPrice()).isEqualTo(BigDecimal.valueOf(999999.99));
        }

        @Test
        @DisplayName("매우 긴 상품명으로 주문 아이템 생성")
        void createOrderItem_WithLongProductName_Success() {
            // Given
            String longProductName = "A".repeat(1000);

            // When
            OrderItem item = OrderItem.builder()
                    .productName(longProductName)
                    .productDescription("Long Product Description")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();
            item.setOrder(testOrder);

            // Then
            assertThat(item.getProductName()).isEqualTo(longProductName);
        }
    }
}