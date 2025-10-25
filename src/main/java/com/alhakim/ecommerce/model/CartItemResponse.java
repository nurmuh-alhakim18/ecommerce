package com.alhakim.ecommerce.model;

import com.alhakim.ecommerce.entity.CartItem;
import com.alhakim.ecommerce.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal weight;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItemResponse fromCartItemAndProduct(CartItem cartItem, Product product) {
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal weight = product.getWeight().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .productId(product.getProductId())
                .productName(product.getName())
                .price(cartItem.getPrice())
                .quantity(cartItem.getQuantity())
                .weight(weight)
                .totalPrice(totalPrice)
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}
