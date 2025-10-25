package com.alhakim.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingOrderRequest {
    private Long orderId;
    private Address fromAddress;
    private Address toAddress;
    private int totalWeightInGrams;

    @Data
    @Builder
    public static class Address {
        private String streetAddress;
        private String city;
        private String state;
        private String postalCode;
    }
}
