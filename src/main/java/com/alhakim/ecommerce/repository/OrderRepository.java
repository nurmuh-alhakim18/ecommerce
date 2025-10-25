package com.alhakim.ecommerce.repository;

import com.alhakim.ecommerce.entity.Order;
import com.alhakim.ecommerce.entity.User;
import com.alhakim.ecommerce.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Query(value = """
    SELECT *
    FROM orders
    WHERE user_id = :userId
    """, nativeQuery = true)
    Page<Order> findByUserIdByPageable(Long userId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    @Query(value = """
        SELECT *
        FROM orders
        WHERE user_id = :userId AND order_date BETWEEN :from AND :to
        """, nativeQuery = true)
    List<Order> findByUserIdAndDateRange(Long userId, LocalDateTime from, LocalDateTime to);

    Optional<Order> findByXenditInvoiceId(String xenditInvoiceId);
    List<Order> findByStatusAndOrderDateBefore(OrderStatus status, LocalDateTime dateTime);
}
