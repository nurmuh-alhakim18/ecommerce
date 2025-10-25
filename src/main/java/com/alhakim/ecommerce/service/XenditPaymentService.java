package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.common.errors.ResourceNotFoundException;
import com.alhakim.ecommerce.common.errors.UserNotFoundException;
import com.alhakim.ecommerce.entity.Order;
import com.alhakim.ecommerce.entity.OrderItem;
import com.alhakim.ecommerce.entity.User;
import com.alhakim.ecommerce.model.OrderStatus;
import com.alhakim.ecommerce.model.PaymentNotification;
import com.alhakim.ecommerce.model.PaymentResponse;
import com.alhakim.ecommerce.repository.OrderItemRepository;
import com.alhakim.ecommerce.repository.OrderRepository;
import com.alhakim.ecommerce.repository.UserRepository;
import com.xendit.exception.XenditException;
import com.xendit.model.Invoice;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class XenditPaymentService implements PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final UserActivityService userActivityService;
    private final OrderItemRepository orderItemRepository;

    @Override
    public PaymentResponse create(Order order) {
        User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new UserNotFoundException("User Not Found"));

        Map<String, Object> params = new HashMap<>();
        params.put("external_id", order.getOrderId().toString());
        params.put("amount", order.getTotalAmount().doubleValue());
        params.put("payer_email", user.getEmail());
        params.put("description", "Payment for order " + order.getOrderId());

        try {
            Invoice invoice = Invoice.create(params);
            return PaymentResponse.builder()
                    .xenditInvoiceId(invoice.getId())
                    .xenditPaymentUrl(invoice.getInvoiceUrl())
                    .xenditExternalId(invoice.getExternalId())
                    .amount(order.getTotalAmount())
                    .xenditInvoiceStatus(invoice.getStatus())
                    .build();
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaymentResponse findByPaymentId(String paymentId) {
        try {
            Invoice invoice = Invoice.getById(paymentId);
            return PaymentResponse.builder()
                    .xenditInvoiceId(invoice.getId())
                    .xenditPaymentUrl(invoice.getInvoiceUrl())
                    .xenditExternalId(invoice.getExternalId())
                    .amount(BigDecimal.valueOf(invoice.getAmount().doubleValue()))
                    .xenditInvoiceStatus(invoice.getStatus())
                    .build();
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verifyByPaymentId(String paymentId) {
        try {
            Invoice invoice = Invoice.getById(paymentId);
            return "PAID".equals(invoice.getStatus());
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleNotification(PaymentNotification paymentNotification) {
        String invoiceId = paymentNotification.getId();
        String status = paymentNotification.getStatus();

        Order order = orderRepository
                .findByXenditInvoiceId(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("order not found"));

        order.setXenditPaymentStatus(status);
        switch (status) {
            case "PAID":
                order.setStatus(OrderStatus.PAID);
                emailService.notifySuccessfulPayment(order);
                trackPurchaseOrder(order);
                break;
            case "EXPIRED":
                order.setStatus(OrderStatus.CANCELLED);
                emailService.notifyFailedPayment(order);
                break;
            case "FAILED":
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                emailService.notifyFailedPayment(order);
                break;
            case "PENDING":
                order.setStatus(OrderStatus.PENDING);
                emailService.notifyFailedPayment(order);
                break;
            default:
        }

        if (paymentNotification.getPaymentMethod() != null) {
            order.setXenditPaymentMethod(paymentNotification.getPaymentMethod());
        }

        orderRepository.save(order);
    }

    @Async
    protected void trackPurchaseOrder(Order order) {
        List<OrderItem> orderItemList = orderItemRepository.findByOrderId(order.getOrderId());
        orderItemList.forEach(orderItem -> {
            userActivityService.trackPurchase(orderItem.getProductId(), order.getUserId());
        });
    }
}
