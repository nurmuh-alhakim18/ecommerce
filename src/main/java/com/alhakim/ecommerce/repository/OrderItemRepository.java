package com.alhakim.ecommerce.repository;

import com.alhakim.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query(value = """
        SELECT *
        FROM order_item AS oi
        JOIN orders AS o ON oi.order_id = o.order_id
        WHERE o.user_id = :userId AND oi.product_id = :productId
        """, nativeQuery = true)
    List<OrderItem> findByUserIdAndProductId(Long userId, Long productId);

    @Query(value = """
    SELECT SUM(quantity * price)
    FROM order_item
    WHERE order_id = :orderId
    """, nativeQuery = true)
    Double calculateTotalOrder(Long orderId);
}
