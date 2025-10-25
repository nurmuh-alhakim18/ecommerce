package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.UserActivity;
import com.alhakim.ecommerce.model.ActivityType;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityService {
    Long getActivityCount(Long productId, ActivityType activityType);
    Long getActivityCountInDateRange(Long productId, ActivityType activityType, LocalDateTime start, LocalDateTime end);
    void trackPurchase(Long productId, Long userId);
    void trackProductView(Long productId, Long userId);
    List<UserActivity> getLastMonthPurchase(Long userId);
    List<UserActivity> getLastMonthUserView(Long userId);
}
