package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.common.errors.BadRequestException;
import com.alhakim.ecommerce.common.errors.ForbiddenAccessException;
import com.alhakim.ecommerce.common.errors.InventoryException;
import com.alhakim.ecommerce.common.errors.ResourceNotFoundException;
import com.alhakim.ecommerce.entity.Cart;
import com.alhakim.ecommerce.entity.CartItem;
import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.model.CartItemResponse;
import com.alhakim.ecommerce.repository.CartItemRepository;
import com.alhakim.ecommerce.repository.CartRepository;
import com.alhakim.ecommerce.repository.ProductRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    public void addItemToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder().userId(userId).build();
            return cartRepository.save(newCart);
        });

        Product product = productRepository
                .findByIdWithPessimisticLocking(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getUserId().equals(userId)) {
            throw new BadRequestException("Can't add your own item to cart");
        }

        if (product.getStockQuantity() <= 0 || product.getStockQuantity() < quantity) {
            throw new InventoryException("Product has no stock");
        }

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), productId);

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cartId(cart.getCartId())
                    .productId(productId)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .build();

            cartItemRepository.save(newItem);
        }

        Gauge.builder("cart_items", this, value -> value.getCartItems(userId).size())
                .description("Number of cart items")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getCartId(), productId);
        if (existingItem.isEmpty()) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        CartItem cartItem = existingItem.get();
        if (quantity <= 0) {
            cartItemRepository.deleteById(cartItem.getCartItemId());
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        Gauge.builder("cart_items", this, value -> value.getCartItems(userId).size())
                .description("Number of cart items")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        Optional<CartItem> existingItem = cartItemRepository.findById(cartItemId);
        if (existingItem.isEmpty()) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        CartItem cartItem = existingItem.get();
        if (!cartItem.getCartId().equals(cart.getCartId())) {
            throw new ForbiddenAccessException("Can't remove item not belong to your cart");
        }

        cartItemRepository.deleteById(cartItemId);

        Gauge.builder("cart_items", this, value -> value.getCartItems(userId).size())
                .description("Number of cart items")
                .register(meterRegistry);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteAllByCartId(cart.getCartId());

        Gauge.builder("cart_items", this, value -> value.getCartItems(userId).size())
                .description("Number of cart items")
                .register(meterRegistry);
    }

    @Override
    public List<CartItemResponse> getCartItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.getUserCartItems(userId);
        if (cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = cartItems.stream().map(CartItem::getProductId).toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));

        return cartItems.stream()
                .map(cartItem -> {
                    Product product = productMap.get(cartItem.getProductId());
                    if (product == null) {
                        throw new ResourceNotFoundException("Product not found");
                    }

                    return CartItemResponse.fromCartItemAndProduct(cartItem, product);
                })
                .toList();
    }
}
