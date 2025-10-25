package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Order;

public interface EmailService {
    void notifySuccessfulPayment(Order order);
    void notifyFailedPayment(Order order);
}
