package com.example.infrastructure.persistence;

import com.example.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(Payment.PaymentStatus status);

    Optional<Payment> findByTransactionId(String transactionId);

    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.order WHERE p.id = :id")
    Optional<Payment> findByIdWithOrder(@Param("id") Long id);

    @Query("SELECT p FROM Payment p WHERE p.order.member.id = :memberId")
    List<Payment> findByMemberId(@Param("memberId") Long memberId);
}