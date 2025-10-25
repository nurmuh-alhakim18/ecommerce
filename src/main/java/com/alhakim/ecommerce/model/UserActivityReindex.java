package com.alhakim.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserActivityReindex {
    private Long productId;
    private Long userId;

    private ActivityType activityType;
}
