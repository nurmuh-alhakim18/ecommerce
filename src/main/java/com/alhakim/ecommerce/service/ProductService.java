package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.model.PaginatedResponse;
import com.alhakim.ecommerce.model.ProductRequest;
import com.alhakim.ecommerce.model.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<ProductResponse> findAllProducts();

    Page<ProductResponse> findByPage(Pageable pageable);

    Page<ProductResponse> findByProductNameAndPageable(String name, Pageable pageable);

    ProductResponse findProductById(Long id);

    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct(Long id, ProductRequest productRequest);

    void deleteProduct(Long id);

    PaginatedResponse convertProductPage(Page<ProductResponse> response);

    Product get(Long id);
}
