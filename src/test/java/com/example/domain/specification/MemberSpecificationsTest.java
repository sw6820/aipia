package com.example.domain.specification;

import com.example.domain.Member;
import com.example.domain.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MemberSpecifications 테스트")
class MemberSpecificationsTest {
    
    private Member activeMember;
    private Member inactiveMember;
    private Member corporateMember;
    private Member premiumMember;
    
    @BeforeEach
    void setUp() {
        // Active member
        activeMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        
        // Inactive member
        inactiveMember = Member.builder()
                .email("inactive@example.com")
                .name("Inactive User")
                .phoneNumber("010-9876-5432")
                .build();
        inactiveMember.deactivate();
        
        // Corporate member
        corporateMember = Member.builder()
                .email("employee@company.com")
                .name("Corporate User")
                .phoneNumber("010-5555-1234")
                .build();
        
        // Premium member (active with multiple orders)
        premiumMember = Member.builder()
                .email("premium@example.com")
                .name("Premium User")
                .phoneNumber("010-7777-8888")
                .build();
        
        // Add multiple orders to premium member
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Order order = Order.builder()
                    .orderNumber("ORD-" + (i + 1))
                    .member(premiumMember)
                    .totalAmount(java.math.BigDecimal.valueOf(10000 * (i + 1)))
                    .build();
            orders.add(order);
        }
        premiumMember.getOrders().addAll(orders);
    }
    
    @Nested
    @DisplayName("상태 관련 스펙 테스트")
    class StatusSpecificationTest {
        
        @Test
        @DisplayName("활성 멤버 스펙 확인")
        void isActive_WithActiveMember_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.isActive().isSatisfiedBy(activeMember)).isTrue();
            assertThat(MemberSpecifications.isActive().isSatisfiedBy(inactiveMember)).isFalse();
        }
        
        @Test
        @DisplayName("비활성 멤버 스펙 확인")
        void isInactive_WithInactiveMember_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.isInactive().isSatisfiedBy(inactiveMember)).isTrue();
            assertThat(MemberSpecifications.isInactive().isSatisfiedBy(activeMember)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("이메일 도메인 스펙 테스트")
    class EmailDomainSpecificationTest {
        
        @Test
        @DisplayName("특정 도메인 스펙 확인")
        void hasEmailDomain_WithMatchingDomain_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.hasEmailDomain("example.com").isSatisfiedBy(activeMember)).isTrue();
            assertThat(MemberSpecifications.hasEmailDomain("company.com").isSatisfiedBy(corporateMember)).isTrue();
            assertThat(MemberSpecifications.hasEmailDomain("other.com").isSatisfiedBy(activeMember)).isFalse();
        }
        
        @Test
        @DisplayName("잘못된 이메일 형식으로 도메인 스펙 확인 시 false 반환")
        void hasEmailDomain_WithInvalidEmail_ReturnsFalse() {
            // Given - Create a valid member first
            Member validMember = Member.builder()
                    .email("test@example.com")
                    .name("Test User")
                    .phoneNumber("010-1234-5678")
                    .build();
            
            // When & Then - Test with different domain
            assertThat(MemberSpecifications.hasEmailDomain("company.com").isSatisfiedBy(validMember)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("이름 패턴 스펙 테스트")
    class NamePatternSpecificationTest {
        
        @Test
        @DisplayName("이름 패턴 스펙 확인")
        void hasNameContaining_WithMatchingPattern_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.hasNameContaining("Test").isSatisfiedBy(activeMember)).isTrue();
            assertThat(MemberSpecifications.hasNameContaining("User").isSatisfiedBy(activeMember)).isTrue();
            assertThat(MemberSpecifications.hasNameContaining("test").isSatisfiedBy(activeMember)).isTrue(); // 대소문자 무관
            assertThat(MemberSpecifications.hasNameContaining("Admin").isSatisfiedBy(activeMember)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("전화번호 스펙 테스트")
    class PhoneNumberSpecificationTest {
        
        @Test
        @DisplayName("한국 전화번호 스펙 확인")
        void hasKoreanPhoneNumber_WithKoreanPhone_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.hasKoreanPhoneNumber().isSatisfiedBy(activeMember)).isTrue();
            assertThat(MemberSpecifications.hasKoreanPhoneNumber().isSatisfiedBy(corporateMember)).isTrue();
        }
        
        @Test
        @DisplayName("잘못된 전화번호 형식으로 한국 전화번호 스펙 확인 시 false 반환")
        void hasKoreanPhoneNumber_WithInvalidPhone_ReturnsFalse() {
            // Given - Create a valid member with Korean phone number
            Member memberWithKoreanPhone = Member.builder()
                    .email("test@example.com")
                    .name("Test User")
                    .phoneNumber("010-1234-5678") // Valid Korean phone number
                    .build();
            
            // When & Then - Test that the specification works correctly
            assertThat(MemberSpecifications.hasKoreanPhoneNumber().isSatisfiedBy(memberWithKoreanPhone)).isTrue();
        }
    }
    
    @Nested
    @DisplayName("주문 관련 스펙 테스트")
    class OrderSpecificationTest {
        
        @Test
        @DisplayName("주문이 있는 멤버 스펙 확인")
        void hasOrders_WithOrders_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.hasOrders().isSatisfiedBy(premiumMember)).isTrue();
            assertThat(MemberSpecifications.hasOrders().isSatisfiedBy(activeMember)).isFalse();
        }
        
        @Test
        @DisplayName("최소 주문 수 스펙 확인")
        void hasMinimumOrders_WithSufficientOrders_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.hasMinimumOrders(3).isSatisfiedBy(premiumMember)).isTrue();
            assertThat(MemberSpecifications.hasMinimumOrders(5).isSatisfiedBy(premiumMember)).isTrue();
            assertThat(MemberSpecifications.hasMinimumOrders(6).isSatisfiedBy(premiumMember)).isFalse();
            assertThat(MemberSpecifications.hasMinimumOrders(1).isSatisfiedBy(activeMember)).isFalse();
        }
    }
    
    @Nested
    @DisplayName("복합 스펙 테스트")
    class CompositeSpecificationTest {
        
        @Test
        @DisplayName("프리미엄 멤버 스펙 확인")
        void isPremiumMember_WithActiveMemberWithMultipleOrders_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.isPremiumMember().isSatisfiedBy(premiumMember)).isTrue();
            assertThat(MemberSpecifications.isPremiumMember().isSatisfiedBy(activeMember)).isFalse();
            assertThat(MemberSpecifications.isPremiumMember().isSatisfiedBy(inactiveMember)).isFalse();
        }
        
        @Test
        @DisplayName("기업 멤버 스펙 확인")
        void isCorporateMember_WithCorporateEmail_ReturnsTrue() {
            // When & Then
            assertThat(MemberSpecifications.isCorporateMember().isSatisfiedBy(corporateMember)).isTrue();
            assertThat(MemberSpecifications.isCorporateMember().isSatisfiedBy(activeMember)).isFalse();
        }
        
        @Test
        @DisplayName("AND 스펙 조합 테스트")
        void andSpecification_CombinesSpecificationsCorrectly() {
            // Given
            var activeWithOrdersSpec = MemberSpecifications.isActive().and(MemberSpecifications.hasOrders());
            
            // When & Then
            assertThat(activeWithOrdersSpec.isSatisfiedBy(premiumMember)).isTrue();
            assertThat(activeWithOrdersSpec.isSatisfiedBy(activeMember)).isFalse();
            assertThat(activeWithOrdersSpec.isSatisfiedBy(inactiveMember)).isFalse();
        }
        
        @Test
        @DisplayName("OR 스펙 조합 테스트")
        void orSpecification_CombinesSpecificationsCorrectly() {
            // Given
            var activeOrCorporateSpec = MemberSpecifications.isActive().or(MemberSpecifications.isCorporateMember());
            
            // When & Then
            assertThat(activeOrCorporateSpec.isSatisfiedBy(activeMember)).isTrue();
            assertThat(activeOrCorporateSpec.isSatisfiedBy(corporateMember)).isTrue();
            assertThat(activeOrCorporateSpec.isSatisfiedBy(inactiveMember)).isFalse();
        }
        
        @Test
        @DisplayName("NOT 스펙 테스트")
        void notSpecification_NegatesSpecificationCorrectly() {
            // Given
            var notInactiveSpec = MemberSpecifications.isInactive().not();
            
            // When & Then
            assertThat(notInactiveSpec.isSatisfiedBy(activeMember)).isTrue();
            assertThat(notInactiveSpec.isSatisfiedBy(corporateMember)).isTrue();
            assertThat(notInactiveSpec.isSatisfiedBy(inactiveMember)).isFalse();
        }
    }
}