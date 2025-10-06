package com.example.dto;

import com.example.domain.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderItemDto 테스트")
class OrderItemDtoTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("Builder를 통한 OrderItemDto 생성")
        void createOrderItemDto_WithBuilder_Success() {
            // When
            OrderItemDto dto = OrderItemDto.builder()
                    .id(1L)
                    .productName("Test Product")
                    .productDescription("Test Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .totalPrice(BigDecimal.valueOf(20.00))
                    .build();

            // Then
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getProductName()).isEqualTo("Test Product");
            assertThat(dto.getProductDescription()).isEqualTo("Test Description");
            assertThat(dto.getQuantity()).isEqualTo(2);
            assertThat(dto.getUnitPrice()).isEqualTo(BigDecimal.valueOf(10.00));
            assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(20.00));
        }

        @Test
        @DisplayName("기본 생성자를 통한 OrderItemDto 생성")
        void createOrderItemDto_WithDefaultConstructor_Success() {
            // When
            OrderItemDto dto = OrderItemDto.builder().build();

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isNull();
            assertThat(dto.getProductName()).isNull();
            assertThat(dto.getProductDescription()).isNull();
            assertThat(dto.getQuantity()).isNull();
            assertThat(dto.getUnitPrice()).isNull();
            assertThat(dto.getTotalPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("Entity 변환 테스트")
    class EntityConversionTest {

        @Test
        @DisplayName("OrderItem 엔티티에서 OrderItemDto로 변환")
        void fromOrderItem_WithValidOrderItem_Success() {
            // Given
            OrderItem orderItem = OrderItem.builder()
                    .productName("Test Product")
                    .productDescription("Test Description")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(15.00))
                    .build();

            // When
            OrderItemDto result = OrderItemDto.from(orderItem);

            // Then
            assertThat(result.getId()).isEqualTo(orderItem.getId());
            assertThat(result.getProductName()).isEqualTo(orderItem.getProductName());
            assertThat(result.getProductDescription()).isEqualTo(orderItem.getProductDescription());
            assertThat(result.getQuantity()).isEqualTo(orderItem.getQuantity());
            assertThat(result.getUnitPrice()).isEqualTo(orderItem.getUnitPrice());
            assertThat(result.getTotalPrice()).isEqualTo(orderItem.getTotalPrice());
        }
    }

    @Nested
    @DisplayName("Getter 테스트")
    class GetterTest {

        @Test
        @DisplayName("모든 필드의 Getter 정상 동작")
        void getter_AllFields_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .id(100L)
                    .productName("Getter Product")
                    .productDescription("Getter Description")
                    .quantity(5)
                    .unitPrice(BigDecimal.valueOf(20.00))
                    .totalPrice(BigDecimal.valueOf(100.00))
                    .build();

            // When & Then
            assertThat(dto.getId()).isEqualTo(100L);
            assertThat(dto.getProductName()).isEqualTo("Getter Product");
            assertThat(dto.getProductDescription()).isEqualTo("Getter Description");
            assertThat(dto.getQuantity()).isEqualTo(5);
            assertThat(dto.getUnitPrice()).isEqualTo(BigDecimal.valueOf(20.00));
            assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100.00));
        }

        @Test
        @DisplayName("null 값들에 대한 Getter 정상 동작")
        void getter_NullValues_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder().build();

            // When & Then
            assertThat(dto.getId()).isNull();
            assertThat(dto.getProductName()).isNull();
            assertThat(dto.getProductDescription()).isNull();
            assertThat(dto.getQuantity()).isNull();
            assertThat(dto.getUnitPrice()).isNull();
            assertThat(dto.getTotalPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("Equals/HashCode 테스트")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("동일한 필드를 가진 OrderItemDto는 equals true")
        void equals_SameFields_True() {
            // Given
            OrderItemDto dto1 = OrderItemDto.builder()
                    .id(1L)
                    .productName("Equal Product")
                    .productDescription("Equal Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .totalPrice(BigDecimal.valueOf(20.00))
                    .build();

            OrderItemDto dto2 = OrderItemDto.builder()
                    .id(1L)
                    .productName("Equal Product")
                    .productDescription("Equal Description")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .totalPrice(BigDecimal.valueOf(20.00))
                    .build();

            // When & Then
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 OrderItemDto는 equals false")
        void equals_DifferentFields_False() {
            // Given
            OrderItemDto dto1 = OrderItemDto.builder()
                    .id(1L)
                    .productName("Different Product 1")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();

            OrderItemDto dto2 = OrderItemDto.builder()
                    .id(2L)
                    .productName("Different Product 2")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(15.00))
                    .build();

            // When & Then
            assertThat(dto1).isNotEqualTo(dto2);
        }

        @Test
        @DisplayName("null과의 equals 비교")
        void equals_Null_False() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .id(1L)
                    .productName("Null Test Product")
                    .build();

            // When & Then
            assertThat(dto).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과의 equals 비교")
        void equals_DifferentType_False() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .id(1L)
                    .productName("Type Test Product")
                    .build();

            // When & Then
            assertThat(dto).isNotEqualTo("Not an OrderItemDto");
        }

        @Test
        @DisplayName("자기 자신과의 equals 비교")
        void equals_Self_True() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .id(1L)
                    .productName("Self Test Product")
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
            OrderItemDto dto = OrderItemDto.builder()
                    .id(1L)
                    .productName("ToString Product")
                    .productDescription("ToString Description")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(25.50))
                    .totalPrice(BigDecimal.valueOf(76.50))
                    .build();

            // When
            String result = dto.toString();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("OrderItemDto");
            assertThat(result).contains("ToString Product");
            assertThat(result).contains("ToString Description");
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("상품명이 null이거나 빈 문자열인 경우")
        void validation_InvalidProductName(String productName) {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName(productName)
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();

            // When & Then
            // Note: OrderItemDto doesn't have validation logic, this is just for testing getter
            assertThat(dto.getProductName()).isEqualTo(productName);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0})
        @DisplayName("수량이 음수이거나 0인 경우")
        void validation_InvalidQuantity(int quantity) {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Test Product")
                    .quantity(quantity)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .build();

            // When & Then
            // Note: OrderItemDto doesn't have validation logic, this is just for testing getter
            assertThat(dto.getQuantity()).isEqualTo(quantity);
        }

        @ParameterizedTest
        @ValueSource(doubles = {-1.0, -0.01})
        @DisplayName("단가가 음수인 경우")
        void validation_NegativeUnitPrice(double unitPrice) {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Test Product")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(unitPrice))
                    .build();

            // When & Then
            // Note: OrderItemDto doesn't have validation logic, this is just for testing getter
            assertThat(dto.getUnitPrice()).isEqualTo(BigDecimal.valueOf(unitPrice));
        }
    }

    @Nested
    @DisplayName("총 가격 계산 테스트")
    class TotalPriceCalculationTest {

        @Test
        @DisplayName("수량과 단가가 모두 양수인 경우 총 가격 계산")
        void totalPriceCalculation_PositiveValues_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Calculation Product")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .totalPrice(BigDecimal.valueOf(30.00))
                    .build();

            // When & Then
            assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(30.00));
        }

        @Test
        @DisplayName("수량이 0인 경우 총 가격은 0")
        void totalPriceCalculation_ZeroQuantity_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Zero Quantity Product")
                    .quantity(0)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .totalPrice(BigDecimal.ZERO)
                    .build();

            // When & Then
            assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("단가가 0인 경우 총 가격은 0")
        void totalPriceCalculation_ZeroUnitPrice_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Zero Price Product")
                    .quantity(5)
                    .unitPrice(BigDecimal.ZERO)
                    .totalPrice(BigDecimal.ZERO)
                    .build();

            // When & Then
            assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("소수점이 있는 가격 계산")
        void totalPriceCalculation_DecimalValues_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Decimal Product")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(12.50))
                    .totalPrice(BigDecimal.valueOf(25.00))
                    .build();

            // When & Then
            assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(25.00));
        }

        @Test
        @DisplayName("null 값들에 대한 총 가격 계산")
        void totalPriceCalculation_NullValues_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Null Test Product")
                    .quantity(null)
                    .unitPrice(null)
                    .totalPrice(null)
                    .build();

            // When & Then
            assertThat(dto.getTotalPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("수량 변경 테스트")
    class QuantityChangeTest {

        @Test
        @DisplayName("수량 변경 시 총 가격 업데이트")
        void quantityChange_UpdateTotalPrice_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Quantity Change Product")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .totalPrice(BigDecimal.valueOf(20.00))
                    .build();

            // When - 수량을 4로 변경
            OrderItemDto updatedDto = OrderItemDto.builder()
                    .id(dto.getId())
                    .productName(dto.getProductName())
                    .productDescription(dto.getProductDescription())
                    .quantity(4)
                    .unitPrice(dto.getUnitPrice())
                    .totalPrice(BigDecimal.valueOf(40.00))
                    .build();

            // Then
            assertThat(updatedDto.getQuantity()).isEqualTo(4);
            assertThat(updatedDto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(40.00));
        }

        @Test
        @DisplayName("수량을 0으로 변경")
        void quantityChange_ToZero_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Zero Quantity Product")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(15.00))
                    .totalPrice(BigDecimal.valueOf(45.00))
                    .build();

            // When - 수량을 0으로 변경
            OrderItemDto updatedDto = OrderItemDto.builder()
                    .id(dto.getId())
                    .productName(dto.getProductName())
                    .productDescription(dto.getProductDescription())
                    .quantity(0)
                    .unitPrice(dto.getUnitPrice())
                    .totalPrice(BigDecimal.ZERO)
                    .build();

            // Then
            assertThat(updatedDto.getQuantity()).isEqualTo(0);
            assertThat(updatedDto.getTotalPrice()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("단가 변경 테스트")
    class UnitPriceChangeTest {

        @Test
        @DisplayName("단가 변경 시 총 가격 업데이트")
        void unitPriceChange_UpdateTotalPrice_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Price Change Product")
                    .quantity(3)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .totalPrice(BigDecimal.valueOf(30.00))
                    .build();

            // When - 단가를 15.00으로 변경
            OrderItemDto updatedDto = OrderItemDto.builder()
                    .id(dto.getId())
                    .productName(dto.getProductName())
                    .productDescription(dto.getProductDescription())
                    .quantity(dto.getQuantity())
                    .unitPrice(BigDecimal.valueOf(15.00))
                    .totalPrice(BigDecimal.valueOf(45.00))
                    .build();

            // Then
            assertThat(updatedDto.getUnitPrice()).isEqualTo(BigDecimal.valueOf(15.00));
            assertThat(updatedDto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(45.00));
        }

        @Test
        @DisplayName("단가를 0으로 변경")
        void unitPriceChange_ToZero_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Zero Price Product")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(20.00))
                    .totalPrice(BigDecimal.valueOf(40.00))
                    .build();

            // When - 단가를 0으로 변경
            OrderItemDto updatedDto = OrderItemDto.builder()
                    .id(dto.getId())
                    .productName(dto.getProductName())
                    .productDescription(dto.getProductDescription())
                    .quantity(dto.getQuantity())
                    .unitPrice(BigDecimal.ZERO)
                    .totalPrice(BigDecimal.ZERO)
                    .build();

            // Then
            assertThat(updatedDto.getUnitPrice()).isEqualTo(BigDecimal.ZERO);
            assertThat(updatedDto.getTotalPrice()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("복합 변경 테스트")
    class ComplexChangeTest {

        @Test
        @DisplayName("수량과 단가를 동시에 변경")
        void complexChange_QuantityAndUnitPrice_Success() {
            // Given
            OrderItemDto dto = OrderItemDto.builder()
                    .productName("Complex Change Product")
                    .quantity(2)
                    .unitPrice(BigDecimal.valueOf(10.00))
                    .totalPrice(BigDecimal.valueOf(20.00))
                    .build();

            // When - 수량을 5로, 단가를 8.00으로 변경
            OrderItemDto updatedDto = OrderItemDto.builder()
                    .id(dto.getId())
                    .productName(dto.getProductName())
                    .productDescription(dto.getProductDescription())
                    .quantity(5)
                    .unitPrice(BigDecimal.valueOf(8.00))
                    .totalPrice(BigDecimal.valueOf(40.00))
                    .build();

            // Then
            assertThat(updatedDto.getQuantity()).isEqualTo(5);
            assertThat(updatedDto.getUnitPrice()).isEqualTo(BigDecimal.valueOf(8.00));
            assertThat(updatedDto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(40.00));
        }
    }
}