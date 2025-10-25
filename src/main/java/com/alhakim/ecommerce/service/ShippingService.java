package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.ShippingOrderRequest;
import com.alhakim.ecommerce.model.ShippingOrderResponse;
import com.alhakim.ecommerce.model.ShippingRateRequest;
import com.alhakim.ecommerce.model.ShippingRateResponse;

import java.math.BigDecimal;

public interface ShippingService {
    ShippingRateResponse calculateShippingRate(ShippingRateRequest shippingRateRequest);
    ShippingOrderResponse createShippingOrder(ShippingOrderRequest shippingOrderRequest);
    String generateAwbNumber(Long orderId);
    BigDecimal calculateTotalWeight(Long orderId);
}
