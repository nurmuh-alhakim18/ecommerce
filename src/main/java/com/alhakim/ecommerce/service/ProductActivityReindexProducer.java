package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.UserActivityReindex;

public interface ProductActivityReindexProducer {
    void publishUserActivityReindex(UserActivityReindex message);
}
