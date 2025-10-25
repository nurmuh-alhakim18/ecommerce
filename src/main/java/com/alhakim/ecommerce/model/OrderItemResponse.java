package com.alhakim.ecommerce.model;

import com.alhakim.ecommerce.entity.OrderItem;
import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.entity.UserAddress;
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
public class OrderItemResponse {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private UserAddressResponse shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderItemResponse fromOrderItemProductAndAddress(OrderItem orderItem, Product product, UserAddress shippingAddress) {
        BigDecimal totalPrice = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));

        return OrderItemResponse.builder()
                .orderItemId(orderItem.getOrderItemId())
                .productId(product.getProductId())
                .productName(product.getName())
                .price(orderItem.getPrice())
                .quantity(orderItem.getQuantity())
                .totalPrice(totalPrice)
                .shippingAddress(UserAddressResponse.fromUserAddress(shippingAddress))
                .createdAt(orderItem.getCreatedAt())
                .updatedAt(orderItem.getUpdatedAt())
                .build();
    }
}
