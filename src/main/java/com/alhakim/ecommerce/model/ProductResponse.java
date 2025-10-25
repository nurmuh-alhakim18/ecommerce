package com.alhakim.ecommerce.model;

import com.alhakim.ecommerce.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String description;
    private Integer stockQuantity;
    private BigDecimal weight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private List<CategoryResponse> categories;

    public static ProductResponse fromProductAndCategories(Product product, List<CategoryResponse> categories) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .stockQuantity(product.getStockQuantity())
                .weight(product.getWeight())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .userId(product.getUserId())
                .categories(categories)
                .build();
    }
}
