package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Order;
import com.alhakim.ecommerce.model.PaymentNotification;
import com.alhakim.ecommerce.model.PaymentResponse;
import com.xendit.exception.XenditException;

public interface PaymentService {
    PaymentResponse create(Order order) throws XenditException;
    PaymentResponse findByPaymentId(String paymentId);
    boolean verifyByPaymentId(String paymentId);
    void handleNotification(PaymentNotification paymentNotification);
}
