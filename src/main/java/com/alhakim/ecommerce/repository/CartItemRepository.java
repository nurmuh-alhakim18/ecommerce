package com.alhakim.ecommerce.repository;

import com.alhakim.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query(value = """
        SELECT ci.*
        FROM cart_item AS ci
        JOIN cart AS c ON ci.cart_id = c.cart_id
        WHERE c.user_id = :userId
        """, nativeQuery = true)
    List<CartItem> getUserCartItems(Long userId);

    @Query(value = """
        SELECT *
        FROM cart_item
        WHERE cart_id = :cartId AND product_id = :productId
        """, nativeQuery = true)
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Modifying
    @Query(value = """
        DELETE FROM cart_item
        WHERE cart_id = :cartId
        """, nativeQuery = true)
    void deleteAllByCartId(Long cartId);
}
