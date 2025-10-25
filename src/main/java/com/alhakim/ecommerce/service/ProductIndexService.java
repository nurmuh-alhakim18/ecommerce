package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.model.ActivityType;

public interface ProductIndexService {
    void reindexProduct(Product product);
    void deleteProduct(Product product);
    String indexName();
    void reindexProductActivity(Long productId, ActivityType activityType, Long value);
}
