package com.alhakim.ecommerce.model;

import com.alhakim.ecommerce.entity.Order;
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
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private BigDecimal subTotal;
    private BigDecimal shippingFee;
    private BigDecimal taxFee;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private String xenditInvoiceId;
    private String xenditPaymentStatus;
    private String xenditPaymentMethod;
    private String paymentUrl;

    public static OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .subTotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .taxFee(order.getTaxFee())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .xenditInvoiceId(order.getXenditInvoiceId())
                .xenditPaymentStatus(order.getXenditPaymentStatus())
                .xenditPaymentMethod(order.getXenditPaymentMethod())
                .build();
    }
}
