package com.alhakim.ecommerce.service;

import java.util.function.Supplier;

public interface RateLimitingService {

    <T> T excecuteWithRateLimit(String key, Supplier<T> supplier);
}
