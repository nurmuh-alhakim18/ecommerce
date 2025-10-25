package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Category;
import com.alhakim.ecommerce.repository.CategoryRepository;
import com.alhakim.ecommerce.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getProductCategories(Long productId) {
        List<Long> categoryIds = productCategoryRepository.findCategoriesByProductId(productId).stream().map(productCategory -> {
            return productCategory.getId().getProductId();
        }).toList();

        return categoryRepository.findAllById(categoryIds);
    }
}
