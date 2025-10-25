package com.alhakim.ecommerce.repository;

import com.alhakim.ecommerce.entity.ProductCategory;
import com.alhakim.ecommerce.entity.ProductCategory.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
    @Query(value = """
        SELECT * 
        FROM product_category
        WHERE product_id = :productId
        """, nativeQuery=true)
    List<ProductCategory> findCategoriesByProductId(Long productId);
}
