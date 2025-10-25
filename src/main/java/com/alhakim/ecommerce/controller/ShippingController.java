package com.alhakim.ecommerce.controller;

import com.alhakim.ecommerce.model.ShippingOrderRequest;
import com.alhakim.ecommerce.model.ShippingOrderResponse;
import com.alhakim.ecommerce.model.ShippingRateRequest;
import com.alhakim.ecommerce.model.ShippingRateResponse;
import com.alhakim.ecommerce.service.ShippingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/shippings")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer")
public class ShippingController {
    private final ShippingService shippingService;

    @PostMapping("/rate")
    public ResponseEntity<ShippingRateResponse> calculateShippingRate(@Valid @RequestBody ShippingRateRequest request) {
        ShippingRateResponse response = shippingService.calculateShippingRate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/order")
    public ResponseEntity<ShippingOrderResponse> createShippingOrder(@Valid @RequestBody ShippingOrderRequest request) {
        ShippingOrderResponse response = shippingService.createShippingOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<String> generateAwbNumber(@PathVariable Long orderId) {
        String awbNumber = shippingService.generateAwbNumber(orderId);
        return ResponseEntity.ok(awbNumber);
    }

    @GetMapping("/weight/{orderId}")
    public ResponseEntity<BigDecimal> calculateTotalWeight(@PathVariable Long orderId) {
        BigDecimal totalWeight = shippingService.calculateTotalWeight(orderId);
        return ResponseEntity.ok(totalWeight);
    }
}
