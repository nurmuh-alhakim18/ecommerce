package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.CartItemResponse;

import java.util.List;

public interface CartService {
    void addItemToCart(Long userId, Long productId, Integer quantity);
    void updateCartItemQuantity(Long userId, Long productId, Integer quantity);
    void removeItemFromCart(Long userId, Long cartItemId);
    void clearCart(Long userId);
    List<CartItemResponse> getCartItems(Long userId);
}
