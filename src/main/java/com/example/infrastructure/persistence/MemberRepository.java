package com.example.infrastructure.persistence;

import com.example.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m WHERE m.email.value = :email")
    Optional<Member> findByEmail(@Param("email") String email);

    List<Member> findByNameContaining(String name);

    List<Member> findByStatus(Member.MemberStatus status);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.orders WHERE m.id = :id")
    Optional<Member> findByIdWithOrders(@Param("id") Long id);

    @Query("SELECT COUNT(m) > 0 FROM Member m WHERE m.email.value = :email")
    boolean existsByEmail(@Param("email") String email);
}