package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getProductCategories(Long productId);
}
