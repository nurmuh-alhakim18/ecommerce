package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.UserActivity;
import com.alhakim.ecommerce.model.ActivityType;
import com.alhakim.ecommerce.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

    private final UserActivityRepository userActivityRepository;
    private final ProductIndexService productIndexService;

    @Override
    public Long getActivityCount(Long productId, ActivityType activityType) {
        return userActivityRepository.countProductActivityByType(productId, String.valueOf(activityType.ordinal()));
    }

    @Override
    public Long getActivityCountInDateRange(Long productId, ActivityType activityType, LocalDateTime start, LocalDateTime end) {
        return userActivityRepository.countProductActivityByTypeAndDateRange(productId, String.valueOf(activityType.ordinal()), start, end);
    }

    @Override
    @Async
    @Transactional
    public void trackPurchase(Long productId, Long userId) {
        UserActivity userActivity = UserActivity.builder()
                .productId(productId)
                .userId(userId)
                .activityType(ActivityType.PURCHASE)
                .build();

        userActivityRepository.save(userActivity);

        Long purchaseCount = getActivityCount(productId, ActivityType.PURCHASE);
        productIndexService.reindexProductActivity(productId, ActivityType.PURCHASE, purchaseCount);
    }

    @Override
    public void trackProductView(Long productId, Long userId) {
        UserActivity userActivity = UserActivity.builder()
                .productId(productId)
                .userId(userId)
                .activityType(ActivityType.VIEW)
                .build();

        userActivityRepository.save(userActivity);
        Long viewCount = getActivityCount(productId, ActivityType.VIEW);
        productIndexService.reindexProductActivity(productId, ActivityType.VIEW, viewCount);
    }

    @Override
    public List<UserActivity> getLastMonthPurchase(Long userId) {
        LocalDateTime from = LocalDateTime.now().minusMonths(1);
        LocalDateTime to = LocalDateTime.now();

        return userActivityRepository.findByUserIdAndType(userId, String.valueOf(ActivityType.PURCHASE.ordinal()), from, to);
    }

    @Override
    public List<UserActivity> getLastMonthUserView(Long userId) {
        LocalDateTime from = LocalDateTime.now().minusMonths(1);
        LocalDateTime to = LocalDateTime.now();

        return userActivityRepository.findByUserIdAndType(userId, String.valueOf(ActivityType.VIEW.ordinal()), from, to);
    }
}
