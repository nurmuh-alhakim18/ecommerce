package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.common.OrderStateTransition;
import com.alhakim.ecommerce.common.errors.ResourceNotFoundException;
import com.alhakim.ecommerce.entity.Order;
import com.alhakim.ecommerce.entity.OrderItem;
import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.model.*;
import com.alhakim.ecommerce.repository.OrderItemRepository;
import com.alhakim.ecommerce.repository.OrderRepository;
import com.alhakim.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(10000);
    private static final BigDecimal RATE_PER_KG = BigDecimal.valueOf(2500);
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    public ShippingRateResponse calculateShippingRate(ShippingRateRequest shippingRateRequest) {
        BigDecimal shippingFee = BASE_RATE.add(shippingRateRequest.getTotalWeightInGrams().divide(BigDecimal.valueOf(1000)).multiply(RATE_PER_KG)).setScale(2, RoundingMode.HALF_UP);
        String estimatedDeliveryTime = "3 - 5 days";
        return ShippingRateResponse.builder()
                .shippingFee(shippingFee)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .build();
    }

    @Override
    public ShippingOrderResponse createShippingOrder(ShippingOrderRequest shippingOrderRequest) {
        String awbNumber = generateAwbNumber(shippingOrderRequest.getOrderId());
        Order order = orderRepository
                .findById(shippingOrderRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!OrderStateTransition.isValidTransition(order.getStatus(), OrderStatus.SHIPPED)) {
            throw new IllegalStateException("Order with current status cannot be shipped");
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setAwbNumber(awbNumber);
        orderRepository.save(order);
        String estimatedDeliveryTime = "3 - 5 days";
        return ShippingOrderResponse.builder()
                .awbNumber(awbNumber)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .build();
    }

    @Override
    public String generateAwbNumber(Long orderId) {
        Random random = new Random();
        String prefix = "awb";
        return String.format("%s%011d", prefix, random.nextInt(100000000));
    }

    @Override
    public BigDecimal calculateTotalWeight(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return orderItems.stream().map(orderItem -> {
            Product product = productRepository
                    .findById(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            return product.getWeight().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
