package com.alhakim.ecommerce.model;

import com.alhakim.ecommerce.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name can't be empty")
    @Size(min = 3, max = 50, message = "Product name should be in between 3-50 characters")
    private String name;

    @NotNull(message = "Price can't be empty")
    @Positive(message = "Price can't be less than 0")
    @Digits(integer = 10, fraction = 2, message = "Price should be 10 digits at max and 2 digits after ,")
    private BigDecimal price;

    @NotNull(message = "Description can't be null")
    @Size(max = 1000, message = "Description exceeded 1000 characters")
    private String description;

    @NotNull(message = "Stock quantity can't be empty")
    @Min(value = 0, message = "Stock quantity should be more than 0")
    private Integer stockQuantity;

    @NotNull(message = "Weight can't be empty")
    @Min(value = 1000, message = "Weight should be at least 1000 grams")
    private BigDecimal weight;

    @NotEmpty(message = "Choose at least one category")
    private List<Long> categoryIds;

    private User user;
}
