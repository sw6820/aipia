package com.example.repository;

import com.example.domain.Member;
import com.example.infrastructure.persistence.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MemberRepository 테스트")
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@example.com")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        testMember = entityManager.persistAndFlush(testMember);
    }

    @Nested
    @DisplayName("기본 CRUD 테스트")
    class BasicCrudTest {

        @Test
        @DisplayName("회원 저장 성공")
        void save_Success() {
            // Given
            Member newMember = Member.builder()
                    .email("new@example.com")
                    .name("New User")
                    .phoneNumber("010-9999-8888")
                    .build();

            // When
            Member savedMember = memberRepository.save(newMember);

            // Then
            assertThat(savedMember.getId()).isNotNull();
            assertThat(savedMember.getEmail()).isEqualTo("new@example.com");
            assertThat(savedMember.getName()).isEqualTo("New User");
            assertThat(savedMember.getPhoneNumber()).isEqualTo("010-9999-8888");
            assertThat(savedMember.getStatus()).isEqualTo(Member.MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("회원 조회 성공")
        void findById_Success() {
            // When
            Optional<Member> foundMember = memberRepository.findById(testMember.getId());

            // Then
            assertThat(foundMember).isPresent();
            assertThat(foundMember.get().getEmail()).isEqualTo("test@example.com");
            assertThat(foundMember.get().getName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("존재하지 않는 회원 조회")
        void findById_NotFound() {
            // When
            Optional<Member> foundMember = memberRepository.findById(999L);

            // Then
            assertThat(foundMember).isEmpty();
        }

        @Test
        @DisplayName("모든 회원 조회")
        void findAll_Success() {
            // Given
            Member member2 = Member.builder()
                    .email("test2@example.com")
                    .name("Test User 2")
                    .phoneNumber("010-2222-2222")
                    .build();
            entityManager.persistAndFlush(member2);

            // When
            List<Member> allMembers = memberRepository.findAll();

            // Then
            assertThat(allMembers).hasSize(2);
            assertThat(allMembers).extracting(Member::getEmail)
                    .containsExactlyInAnyOrder("test@example.com", "test2@example.com");
        }

        @Test
        @DisplayName("회원 삭제 성공")
        void delete_Success() {
            // When
            memberRepository.delete(testMember);
            entityManager.flush();

            // Then
            Optional<Member> deletedMember = memberRepository.findById(testMember.getId());
            assertThat(deletedMember).isEmpty();
        }
    }

    @Nested
    @DisplayName("이메일 기반 조회 테스트")
    class EmailBasedQueryTest {

        @Test
        @DisplayName("이메일로 회원 조회 성공")
        void findByEmail_Success() {
            // When
            Optional<Member> foundMember = memberRepository.findByEmail("test@example.com");

            // Then
            assertThat(foundMember).isPresent();
            assertThat(foundMember.get().getEmail()).isEqualTo("test@example.com");
            assertThat(foundMember.get().getName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회")
        void findByEmail_NotFound() {
            // When
            Optional<Member> foundMember = memberRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(foundMember).isEmpty();
        }

        @Test
        @DisplayName("이메일 존재 여부 확인 - 존재함")
        void existsByEmail_Exists() {
            // When
            boolean exists = memberRepository.existsByEmail("test@example.com");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("이메일 존재 여부 확인 - 존재하지 않음")
        void existsByEmail_NotExists() {
            // When
            boolean exists = memberRepository.existsByEmail("nonexistent@example.com");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("대소문자 구분 없는 이메일 조회")
        void findByEmail_CaseInsensitive() {
            // When
            Optional<Member> foundMember = memberRepository.findByEmail("TEST@EXAMPLE.COM");

            // Then
            assertThat(foundMember).isEmpty(); // H2는 기본적으로 대소문자를 구분함
        }
    }

    @Nested
    @DisplayName("이름 기반 검색 테스트")
    class NameBasedSearchTest {

        @BeforeEach
        void setUp() {
            Member member1 = Member.builder()
                    .email("john@example.com")
                    .name("John Doe")
                    .phoneNumber("010-1111-1111")
                    .build();
            entityManager.persistAndFlush(member1);

            Member member2 = Member.builder()
                    .email("jane@example.com")
                    .name("Jane Smith")
                    .phoneNumber("010-2222-2222")
                    .build();
            entityManager.persistAndFlush(member2);

            Member member3 = Member.builder()
                    .email("bob@example.com")
                    .name("Bob Johnson")
                    .phoneNumber("010-3333-3333")
                    .build();
            entityManager.persistAndFlush(member3);
        }

        @Test
        @DisplayName("이름으로 부분 검색 성공")
        void findByNameContaining_Success() {
            // When
            List<Member> members = memberRepository.findByNameContaining("John");

            // Then
            assertThat(members).hasSize(2);
            assertThat(members).extracting(Member::getName)
                    .containsExactlyInAnyOrder("John Doe", "Bob Johnson");
        }

        @Test
        @DisplayName("존재하지 않는 이름으로 검색")
        void findByNameContaining_NotFound() {
            // When
            List<Member> members = memberRepository.findByNameContaining("Alice");

            // Then
            assertThat(members).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열로 이름 검색")
        void findByNameContaining_EmptyString() {
            // When
            List<Member> members = memberRepository.findByNameContaining("");

            // Then
            assertThat(members).hasSize(4); // 모든 회원 반환
        }

        @Test
        @DisplayName("공백으로 이름 검색")
        void findByNameContaining_Whitespace() {
            // When
            List<Member> members = memberRepository.findByNameContaining(" ");

            // Then
            assertThat(members).hasSize(4); // 공백이 포함된 이름들 (testMember "Test User" + 3 from setUp)
        }
    }

    @Nested
    @DisplayName("상태 기반 조회 테스트")
    class StatusBasedQueryTest {

        @BeforeEach
        void setUp() {
            Member activeMember = Member.builder()
                    .email("active@example.com")
                    .name("Active User")
                    .phoneNumber("010-1111-1111")
                    .build();
            entityManager.persistAndFlush(activeMember);

            Member inactiveMember = Member.builder()
                    .email("inactive@example.com")
                    .name("Inactive User")
                    .phoneNumber("010-2222-2222")
                    .build();
            inactiveMember.deactivate();
            entityManager.persistAndFlush(inactiveMember);
        }

        @Test
        @DisplayName("활성 회원 조회")
        void findByStatus_Active() {
            // When
            List<Member> activeMembers = memberRepository.findByStatus(Member.MemberStatus.ACTIVE);

            // Then
            assertThat(activeMembers).hasSize(2);
            assertThat(activeMembers).extracting(Member::getStatus)
                    .containsOnly(Member.MemberStatus.ACTIVE);
        }

        @Test
        @DisplayName("비활성 회원 조회")
        void findByStatus_Inactive() {
            // When
            List<Member> inactiveMembers = memberRepository.findByStatus(Member.MemberStatus.INACTIVE);

            // Then
            assertThat(inactiveMembers).hasSize(1);
            assertThat(inactiveMembers).extracting(Member::getStatus)
                    .containsOnly(Member.MemberStatus.INACTIVE);
            assertThat(inactiveMembers.get(0).getEmail()).isEqualTo("inactive@example.com");
        }
    }

    @Nested
    @DisplayName("연관 관계 조회 테스트")
    class RelationshipQueryTest {

        @Test
        @DisplayName("주문과 함께 회원 조회 성공")
        void findByIdWithOrders_Success() {
            // Given
            Member memberWithOrders = Member.builder()
                    .email("orders@example.com")
                    .name("User With Orders")
                    .phoneNumber("010-9999-9999")
                    .build();
            memberWithOrders = entityManager.persistAndFlush(memberWithOrders);

            // When
            Optional<Member> foundMember = memberRepository.findByIdWithOrders(memberWithOrders.getId());

            // Then
            assertThat(foundMember).isPresent();
            assertThat(foundMember.get().getEmail()).isEqualTo("orders@example.com");
            assertThat(foundMember.get().getOrders()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 회원의 주문과 함께 조회")
        void findByIdWithOrders_NotFound() {
            // When
            Optional<Member> foundMember = memberRepository.findByIdWithOrders(999L);

            // Then
            assertThat(foundMember).isEmpty();
        }
    }

    @Nested
    @DisplayName("데이터 무결성 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("중복 이메일 저장 시 예외 발생")
        void save_DuplicateEmail_ThrowsException() {
            // Given
            Member duplicateMember = Member.builder()
                    .email("test@example.com") // 이미 존재하는 이메일
                    .name("Duplicate User")
                    .phoneNumber("010-9999-9999")
                    .build();

            // When & Then
            assertThatThrownBy(() -> {
                memberRepository.save(duplicateMember);
                entityManager.flush();
            }).isInstanceOf(Exception.class); // JPA 예외 발생
        }

        @Test
        @DisplayName("null 이메일 저장 시 예외 발생")
        void save_NullEmail_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> {
                Member memberWithNullEmail = Member.builder()
                        .email(null)
                        .name("Null Email User")
                        .phoneNumber("010-9999-9999")
                        .build();
            }).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 이름 저장 시 예외 발생")
        void save_NullName_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> {
                Member memberWithNullName = Member.builder()
                        .email("nullname@example.com")
                        .name(null)
                        .phoneNumber("010-9999-9999")
                        .build();
            }).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null 전화번호 저장 시 예외 발생")
        void save_NullPhoneNumber_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> {
                Member memberWithNullPhone = Member.builder()
                        .email("nullphone@example.com")
                        .name("Null Phone User")
                        .phoneNumber(null)
                        .build();
            }).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTest {

        @Test
        @DisplayName("대량 데이터 조회 성능 테스트")
        void findAll_LargeDataset_Performance() {
            // Given
            for (int i = 0; i < 100; i++) {
                Member member = Member.builder()
                        .email("perf" + i + "@example.com")
                        .name("Performance User " + i)
                        .phoneNumber("010-" + String.format("%04d", i) + "-" + String.format("%04d", i))
                        .build();
                entityManager.persist(member);
            }
            entityManager.flush();

            // When
            long startTime = System.currentTimeMillis();
            List<Member> allMembers = memberRepository.findAll();
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(allMembers).hasSize(101); // 100 + 기존 1개
            assertThat(endTime - startTime).isLessThan(1000); // 1초 이내
        }

        @Test
        @DisplayName("인덱스 활용 이메일 조회 성능 테스트")
        void findByEmail_IndexPerformance() {
            // Given
            for (int i = 0; i < 1000; i++) {
                Member member = Member.builder()
                        .email("index" + i + "@example.com")
                        .name("Index User " + i)
                        .phoneNumber("010-" + String.format("%04d", i) + "-" + String.format("%04d", i))
                        .build();
                entityManager.persist(member);
            }
            entityManager.flush();

            // When
            long startTime = System.currentTimeMillis();
            Optional<Member> foundMember = memberRepository.findByEmail("index500@example.com");
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(foundMember).isPresent();
            assertThat(endTime - startTime).isLessThan(100); // 100ms 이내
        }
    }
}