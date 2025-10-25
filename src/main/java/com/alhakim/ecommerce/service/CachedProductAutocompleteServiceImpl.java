package com.alhakim.ecommerce.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CachedProductAutocompleteServiceImpl implements CachedProductAutocompleteService {

    private final SearchService searchService;
    private final CacheService cacheService;

    @Value("${suggestion.cache.ttl}")
    private Duration ttl;

    @Override
    public List<String> getAutocomplete(String query) {
        String cacheKey = "product:suggestions:" + query;
        return cacheService.get(cacheKey, new TypeReference<List<String>>() {}).orElseGet(() -> {
            List<String> autocomplete = searchService.getAutocomplete(query);
            cacheService.put(cacheKey, autocomplete, ttl);
            return autocomplete;
        });
    }

    @Override
    public List<String> getNgramAutocomplete(String query) {
        String cacheKey = "product:ngram:suggestions:" + query;
        return cacheService.get(cacheKey, new TypeReference<List<String>>() {}).orElseGet(() -> {
            List<String> autocomplete = searchService.getNgramAutocomplete(query);
            cacheService.put(cacheKey, autocomplete, ttl);
            return autocomplete;
        });
    }

    @Override
    public List<String> getFuzzyAutocomplete(String query) {
        String cacheKey = "product:fuzzy:suggestions:" + query;
        return cacheService.get(cacheKey, new TypeReference<List<String>>() {}).orElseGet(() -> {
            List<String> autocomplete = searchService.getFuzzyAutocomplete(query);
            cacheService.put(cacheKey, autocomplete, ttl);
            return autocomplete;
        });
    }

    @Override
    public List<String> combinedAutocomplete(String query) {
        String cacheKey = "product:combined:suggestions:" + query;
        return cacheService.get(cacheKey, new TypeReference<List<String>>() {}).orElseGet(() -> {
            List<String> autocomplete = searchService.combinedAutocomplete(query);
            cacheService.put(cacheKey, autocomplete, ttl);
            return autocomplete;
        });
    }
}
