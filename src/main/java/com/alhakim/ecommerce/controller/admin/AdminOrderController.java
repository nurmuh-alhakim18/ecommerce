package com.alhakim.ecommerce.controller.admin;

import com.alhakim.ecommerce.common.PageUtil;
import com.alhakim.ecommerce.common.errors.ForbiddenAccessException;
import com.alhakim.ecommerce.common.errors.ResourceNotFoundException;
import com.alhakim.ecommerce.entity.Order;
import com.alhakim.ecommerce.model.OrderResponse;
import com.alhakim.ecommerce.model.PaginatedResponse;
import com.alhakim.ecommerce.model.UserInfo;
import com.alhakim.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class AdminOrderController {

    private final OrderService orderService;

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
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        Order order = orderService.findOrderById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return ResponseEntity.ok(OrderResponse.fromOrder(order));
    }
}
