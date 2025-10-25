package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.ProductReindex;

public interface ProductReindexProducer {
    void publishProductReindex(ProductReindex message);
}
