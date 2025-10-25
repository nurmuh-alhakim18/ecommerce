package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.*;

import java.util.List;

public interface SearchService {
    SearchResponse<ProductResponse> search(ProductSearchRequest searchRequest);
    SearchResponse<ProductResponse> similarProducts(Long productId);
    SearchResponse<ProductResponse> userRecommendation(Long userId, ActivityType activityType);
    List<String> getAutocomplete(String query);
    List<String> getNgramAutocomplete(String query);
    List<String> getFuzzyAutocomplete(String query);
    List<String> combinedAutocomplete(String query);
}
