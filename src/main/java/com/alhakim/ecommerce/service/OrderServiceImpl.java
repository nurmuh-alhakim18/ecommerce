package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.common.OrderStateTransition;
import com.alhakim.ecommerce.common.errors.InventoryException;
import com.alhakim.ecommerce.common.errors.ResourceNotFoundException;
import com.alhakim.ecommerce.entity.*;
import com.alhakim.ecommerce.model.*;
import com.alhakim.ecommerce.repository.*;
import com.xendit.exception.XenditException;
import com.xendit.model.Invoice;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserAddressRepository userAddressRepository;
    private final ProductRepository productRepository;
    private final ShippingService shippingService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.03);

    private final MeterRegistry meterRegistry;
    private Counter checkoutCounter;
    private DistributionSummary orderValueSummary;

    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest checkoutRequest) {
        List<CartItem> selectedItems = cartItemRepository.findAllById(checkoutRequest.getSelectedCartItemIds());
        if (selectedItems.isEmpty()) {
            throw new ResourceNotFoundException("No selected cart items found");
        }

        UserAddress shippingAddress = userAddressRepository
                .findById(checkoutRequest.getUserAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("User address not found"));

        Map<Long, Integer> productQuantities = selectedItems.stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));

        if (!inventoryService.checkAndLockInventory(productQuantities)) {
            throw new InventoryException("Insufficient inventory");
        }

        Order newOrder = Order.builder()
                .userId(checkoutRequest.getUserId())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .taxFee(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .build();

        Order savedOrder = orderRepository.save(newOrder);
        List<OrderItem> orderItems = selectedItems.stream().map(cartItem -> {
            return OrderItem.builder()
                    .orderId(savedOrder.getOrderId())
                    .productId(cartItem.getProductId())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .userAddressId(shippingAddress.getUserAddressId())
                    .build();
        }).toList();

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteAll(selectedItems);
        BigDecimal subTotal = orderItems.stream().map(orderItem -> {
            return orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = orderItems.stream().map(orderItem -> {
            Optional<Product> product = productRepository.findById(orderItem.getProductId());
            if (product.isEmpty()) {
                return BigDecimal.ZERO;
            }

            Optional<UserAddress> sellerAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(product.get().getUserId());
            if (sellerAddress.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal totalWeight = product.get().getWeight().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            ShippingRateRequest rateRequest = ShippingRateRequest.builder()
                    .totalWeightInGrams(totalWeight)
                    .fromAddress(ShippingRateRequest.fromUserAddress(sellerAddress.get()))
                    .toAddress(ShippingRateRequest.fromUserAddress(shippingAddress))
                    .build();

            ShippingRateResponse rateResponse = shippingService.calculateShippingRate(rateRequest);
            return rateResponse.getShippingFee();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subTotal.multiply(TAX_RATE);
        BigDecimal totalAmount = subTotal.add(tax).add(shippingFee);

        savedOrder.setSubtotal(subTotal);
        savedOrder.setShippingFee(shippingFee);
        savedOrder.setTaxFee(tax);
        savedOrder.setTotalAmount(totalAmount);

        orderRepository.save(savedOrder);

        String paymentUrl;
        try {
            PaymentResponse paymentResponse = paymentService.create(savedOrder);
            savedOrder.setXenditInvoiceId(paymentResponse.getXenditInvoiceId());
            savedOrder.setXenditPaymentStatus(paymentResponse.getXenditInvoiceStatus());
            paymentUrl = paymentResponse.getXenditPaymentUrl();

            orderRepository.save(savedOrder);
            inventoryService.decreaseQuantity(productQuantities);
        } catch (Exception e) {
            log.error("Payment creation is failed: {}", e.getMessage());
            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(savedOrder);
            return OrderResponse.fromOrder(savedOrder);
        }

        OrderResponse orderResponse = OrderResponse.fromOrder(savedOrder);
        orderResponse.setPaymentUrl(paymentUrl);

        if (checkoutCounter == null) {
            checkoutCounter = Counter.builder("checkout.count")
                    .description("Number of checkouts created")
                    .register(meterRegistry);
        }

        if (orderValueSummary == null) {
            orderValueSummary = DistributionSummary.builder("order.value")
                    .description("Order value in rupiah")
                    .baseUnit("Rupiah")
                    .register(meterRegistry);
        }

        checkoutCounter.increment();
        orderValueSummary.record(totalAmount.doubleValue());

        return orderResponse;
    }

    @Override
    public Optional<Order> findOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> findOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Page<OrderResponse> findOrdersByUserIdAndPageable(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdByPageable(userId, pageable).map(OrderResponse::fromOrder);
    }

    @Override
    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!OrderStateTransition.isValidTransition(order.getStatus(), OrderStatus.CANCELLED)) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        Map<Long, Integer> productQuantities = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        if (order.getStatus().equals(OrderStatus.CANCELLED)) {
            cancelXenditInvoice(order);
            inventoryService.increaseQuantity(productQuantities);
        }
    }

    @Override
    public List<OrderItemResponse> findOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
        List<Long> shippingAddressIds = orderItems.stream().map(OrderItem::getUserAddressId).toList();

        List<Product> products = productRepository.findAllById(productIds);
        List<UserAddress> shippingAddress = userAddressRepository.findAllById(shippingAddressIds);

        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getProductId, Function.identity()));
        Map<Long, UserAddress> userAddressMap = shippingAddress.stream().collect(Collectors.toMap(UserAddress::getUserId, Function.identity()));

        return orderItems.stream().map(orderItem -> {
            Product product = productMap.get(orderItem.getProductId());
            UserAddress userAddress = userAddressMap.get(orderItem.getUserAddressId());

            if (product == null || userAddress == null) {
                throw new ResourceNotFoundException("Product or User Address not found");
            }

            return OrderItemResponse.fromOrderItemProductAndAddress(orderItem, product, userAddress);
        }).toList();
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!OrderStateTransition.isValidTransition(order.getStatus(), newStatus)) {
            throw new IllegalStateException("Order with current status cannot be updated to " + newStatus);
        }

        if (newStatus.equals(OrderStatus.CANCELLED)) {
            cancelXenditInvoice(order);
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            Map<Long, Integer> productQuantities = orderItems.stream()
                    .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));
            inventoryService.increaseQuantity(productQuantities);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public Double calculateOrderTotal(Long orderId) {
        return orderItemRepository.calculateTotalOrder(orderId);
    }

    @Override
    public PaginatedResponse<OrderResponse> convertOrderPage(Page<OrderResponse> orderPage) {
        return PaginatedResponse.<OrderResponse>builder()
                .data(orderPage.getContent())
                .pageNo(orderPage.getNumber() + 1)
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    private void cancelXenditInvoice(Order order) {
        try {
            Invoice invoice = Invoice.expire(order.getXenditInvoiceId());
            order.setXenditPaymentStatus(invoice.getStatus());
            orderRepository.save(order);
        } catch (XenditException e) {
            log.error("Xendit invoice cancellation failed: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void cancelUnpaidOrders( ) {
        LocalDateTime cancelThreshold = LocalDateTime.now().minusDays(1);
        List<Order> unpaidOrders = orderRepository.findByStatusAndOrderDateBefore(OrderStatus.PENDING, cancelThreshold);
        for (Order order: unpaidOrders) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.saveAll(unpaidOrders);
        unpaidOrders.forEach(this::cancelXenditInvoice);
    }
}

