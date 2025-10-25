package com.alhakim.ecommerce.controller;

import com.alhakim.ecommerce.common.PageUtil;
import com.alhakim.ecommerce.common.errors.BadRequestException;
import com.alhakim.ecommerce.common.errors.ForbiddenAccessException;
import com.alhakim.ecommerce.common.errors.ResourceNotFoundException;
import com.alhakim.ecommerce.entity.Order;
import com.alhakim.ecommerce.model.*;
import com.alhakim.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest checkoutRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        checkoutRequest.setUserId(userInfo.getUser().getUserId());
        OrderResponse orderResponse = orderService.checkout(checkoutRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        Order order = orderService.findOrderById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUserId().equals(userInfo.getUser().getUserId())) {
            throw new ForbiddenAccessException("You do not have permission to access this resource");
        }

        return ResponseEntity.ok(OrderResponse.fromOrder(order));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<OrderResponse>> findOrdersByUserId(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "order_id,desc") String[] sort
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        List<Sort.Order> orders = PageUtil.parseSortOrderRequest(sort);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(orders));

        Page<OrderResponse> userOrders = orderService.findOrdersByUserIdAndPageable(userInfo.getUser().getUserId(), pageable);
        PaginatedResponse<OrderResponse> paginatedResponse = orderService.convertOrderPage(userOrders);
        return ResponseEntity.ok(paginatedResponse);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        Order order = orderService.findOrderById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUserId().equals(userInfo.getUser().getUserId())) {
            throw new ForbiddenAccessException("You do not have permission to access this resource");
        }
        
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemResponse>> findOrderItems(@PathVariable Long orderId) {
        List<OrderItemResponse> orderItemResponse = orderService.findOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(orderItemResponse);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId, @RequestParam String newStatus) {
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown status: " + newStatus);
        }

        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/total")
    public ResponseEntity<Double> calculateOrderTotal(@PathVariable Long orderId) {
        double total = orderService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(total);
    }
}
