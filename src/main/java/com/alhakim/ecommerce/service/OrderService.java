package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Order;
import com.alhakim.ecommerce.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    OrderResponse checkout(CheckoutRequest checkoutRequest);

    Optional<Order> findOrderById(Long orderId);

    List<Order> findOrdersByUserId(Long userId);

    Page<OrderResponse> findOrdersByUserIdAndPageable(Long userId, Pageable pageable);

    List<Order> findOrdersByStatus(OrderStatus status);

    void cancelOrder(Long orderId);

    List<OrderItemResponse> findOrderItemsByOrderId(Long orderId);

    void updateOrderStatus(Long orderId, OrderStatus newStatus);

    Double calculateOrderTotal(Long orderId);

    PaginatedResponse<OrderResponse> convertOrderPage(Page<OrderResponse> orderPage);
}
