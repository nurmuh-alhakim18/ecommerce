package com.alhakim.ecommerce.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@JsonNaming(SnakeCaseStrategy.class)
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private long totalPages;
    private boolean last;
}
