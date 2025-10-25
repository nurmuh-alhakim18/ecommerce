package com.alhakim.ecommerce.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    private Long userId;

    @Size(min = 1, message = "At least one cart item to checkout")
    private List<Long> selectedCartItemIds;

    @NotNull(message = "User address Id is required")
    private Long userAddressId;
}
