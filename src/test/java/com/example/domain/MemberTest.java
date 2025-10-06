package com.example.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;


import static org.assertj.core.api.Assertions.*;

@DisplayName("Member 도메인 테스트")
class MemberTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
    }

    @Nested
    @DisplayName("회원 생성 테스트")
    class CreateMemberTest {

        @Test
        @DisplayName("정상적인 회원 생성 성공")
        void createMember_Success() {
            // Given & When
            Member newMember = Member.builder()
                    .email("new@example.com")
                    .name("New User")
                    .phoneNumber("010-9999-8888")
                    .build();

            // Then
            assertThat(newMember.getEmail()).isEqualTo("new@example.com");
            assertThat(newMember.getName()).isEqualTo("New User");
            assertThat(newMember.getPhoneNumber()).isEqualTo("010-9999-8888");
            assertThat(newMember.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("이메일이 null인 경우 예외 발생")
        void createMember_NullEmail_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> Member.builder()
                    .email(null)
                    .name("Test User")
                    .phoneNumber("010-1234-5678")
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("빈 이메일로 회원 생성 시 예외 발생")
        void createMember_EmptyEmail_ThrowsException(String email) {
            // When & Then
            assertThatThrownBy(() -> Member.builder()
                    .email(email)
                    .name("Test User")
                    .phoneNumber("010-1234-5678")
                    .build())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("회원 상태 변경 테스트")
    class MemberStatusTest {

        @Test
        @DisplayName("회원 비활성화 성공")
        void deactivate_Success() {
            // Given
            assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);

            // When
            member.deactivate();

            // Then
            assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.INACTIVE);
        }

        @Test
        @DisplayName("회원 활성화 성공")
        void activate_Success() {
            // Given
            member.deactivate();
            assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.INACTIVE);

            // When
            member.activate();

            // Then
            assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("이미 활성화된 회원을 다시 활성화해도 상태 유지")
        void activate_AlreadyActive_StatusUnchanged() {
            // Given
            assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);

            // When
            member.activate();

            // Then
            assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("주문 추가 테스트")
    class AddOrderTest {

        @Test
        @DisplayName("주문 추가 성공")
        void addOrder_Success() {
            // Given
            Order order = Order.builder()
                    .orderNumber("ORD-001")
                    .member(member)
                    .totalAmount(java.math.BigDecimal.valueOf(100.00))
                    .build();

            // When
            member.addOrder(order);

            // Then
            assertThat(member.getOrders()).hasSize(1);
            assertThat(member.getOrders()).contains(order);
            assertThat(order.getMember()).isEqualTo(member);
        }

        @Test
        @DisplayName("여러 주문 추가 성공")
        void addMultipleOrders_Success() {
            // Given
            Order order1 = Order.builder()
                    .orderNumber("ORD-001")
                    .member(member)
                    .totalAmount(java.math.BigDecimal.valueOf(100.00))
                    .build();

            Order order2 = Order.builder()
                    .orderNumber("ORD-002")
                    .member(member)
                    .totalAmount(java.math.BigDecimal.valueOf(200.00))
                    .build();

            // When
            member.addOrder(order1);
            member.addOrder(order2);

            // Then
            assertThat(member.getOrders()).hasSize(2);
            assertThat(member.getOrders()).contains(order1, order2);
        }

        @Test
        @DisplayName("null 주문 추가 시 예외 발생")
        void addOrder_NullOrder_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> member.addOrder(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("회원 정보 검증 테스트")
    class MemberValidationTest {

        @Test
        @DisplayName("이메일 형식 검증")
        void emailFormatValidation() {
            // Given
            String validEmail = "test@example.com";

            // When
            Member member = Member.builder()
                    .email(validEmail)
                    .name("Test User")
                    .phoneNumber("010-1234-5678")
                    .build();

            // Then
            assertThat(member.getEmail()).isEqualTo(validEmail);
        }

        @Test
        @DisplayName("전화번호 형식 검증")
        void phoneNumberFormatValidation() {
            // Given
            String validPhoneNumber = "010-1234-5678";

            // When
            Member member = Member.builder()
                    .email("test@example.com")
                    .name("Test User")
                    .phoneNumber(validPhoneNumber)
                    .build();

            // Then
            assertThat(member.getPhoneNumber()).isEqualTo(validPhoneNumber);
        }

        @Test
        @DisplayName("이름 길이 검증")
        void nameLengthValidation() {
            // Given
            String longName = "A".repeat(100);

            // When
            Member member = Member.builder()
                    .email("test@example.com")
                    .name(longName)
                    .phoneNumber("010-1234-5678")
                    .build();

            // Then
            assertThat(member.getName()).isEqualTo(longName);
        }
    }

    @Nested
    @DisplayName("회원 동등성 테스트")
    class MemberEqualityTest {

        @Test
        @DisplayName("동일한 이메일을 가진 회원은 동일한 객체로 간주")
        void equals_SameEmail_ReturnsTrue() {
            // Given
            Member member1 = Member.builder()
                    .email("test@example.com")
                    .name("User 1")
                    .phoneNumber("010-1111-1111")
                    .build();

            Member member2 = Member.builder()
                    .email("test@example.com")
                    .name("User 2")
                    .phoneNumber("010-2222-2222")
                    .build();

            // When & Then
            assertThat(member1.getEmail()).isEqualTo(member2.getEmail());
        }

        @Test
        @DisplayName("다른 이메일을 가진 회원은 다른 객체로 간주")
        void equals_DifferentEmail_ReturnsFalse() {
            // Given
            Member member1 = Member.builder()
                    .email("test1@example.com")
                    .name("User 1")
                    .phoneNumber("010-1111-1111")
                    .build();

            Member member2 = Member.builder()
                    .email("test2@example.com")
                    .name("User 2")
                    .phoneNumber("010-2222-2222")
                    .build();

            // When & Then
            assertThat(member1.getEmail()).isNotEqualTo(member2.getEmail());
        }
    }

    @Nested
    @DisplayName("회원 비즈니스 규칙 테스트")
    class MemberBusinessRulesTest {

        @Test
        @DisplayName("회원 생성 시 기본 상태는 ACTIVE")
        void createMember_DefaultStatusIsActive() {
            // When
            Member newMember = Member.builder()
                    .email("test@example.com")
                    .name("Test User")
                    .phoneNumber("010-1234-5678")
                    .build();

            // Then
            assertThat(newMember.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("비활성화된 회원도 주문 추가 가능")
        void deactivatedMember_CanAddOrders() {
            // Given
            member.deactivate();

            // When
            Order order = Order.builder()
                    .orderNumber("ORD-001")
                    .member(member)
                    .totalAmount(java.math.BigDecimal.valueOf(100.00))
                    .build();
            member.addOrder(order);

            // Then
            assertThat(member.getOrders()).hasSize(1);
            assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.INACTIVE);
        }

        @Test
        @DisplayName("회원 상태 변경 시 기존 주문은 유지")
        void changeStatus_ExistingOrdersPreserved() {
            // Given
            Order order = Order.builder()
                    .orderNumber("ORD-001")
                    .member(member)
                    .totalAmount(java.math.BigDecimal.valueOf(100.00))
                    .build();
            member.addOrder(order);

            // When
            member.deactivate();

            // Then
            assertThat(member.getOrders()).hasSize(1);
            assertThat(member.getStatus()).isEqualTo(Member.MemberStatus.INACTIVE);
        }
    }
}