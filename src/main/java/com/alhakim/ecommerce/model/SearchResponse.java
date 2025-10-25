package com.alhakim.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse<T> {
    private List<T> data;
    private Long totalHits;
    private Map<String, List<FacetEntry>> facets;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FacetEntry {
        private String key;
        private Long docCount;
    }
}
