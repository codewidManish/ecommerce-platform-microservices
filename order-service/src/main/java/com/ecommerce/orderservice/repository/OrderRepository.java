package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        LEFT JOIN FETCH o.items
        LEFT JOIN FETCH o.statusHistory
        WHERE o.id = :id AND o.userId = :userId
        """)
    Optional<Order> findByIdAndUserIdWithDetails(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
          AND o.createdAt BETWEEN :start AND :end
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByStatusAndDateRange(
        @Param("status") Order.OrderStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    long countByUserIdAndStatus(Long userId, Order.OrderStatus status);
}
