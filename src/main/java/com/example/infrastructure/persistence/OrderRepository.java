package com.example.infrastructure.persistence;

import com.example.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMemberId(Long memberId);

    List<Order> findByStatus(Order.OrderStatus status);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems LEFT JOIN FETCH o.payment WHERE o.id = :id")
    Optional<Order> findByIdWithOrderItemsAndPayment(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.member WHERE o.id = :id")
    Optional<Order> findByIdWithMember(@Param("id") Long id);

    @Query("SELECT o FROM Order o WHERE o.member.id = :memberId AND o.status = :status")
    List<Order> findByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") Order.OrderStatus status);
}