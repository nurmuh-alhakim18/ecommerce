package com.alhakim.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchRequest {
    private String query;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    private String sortBy = "_score";
    private String sortOrder;
    private Integer page;
    private Integer size;
}
