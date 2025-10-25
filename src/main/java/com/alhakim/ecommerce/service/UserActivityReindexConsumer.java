package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Product;
import com.alhakim.ecommerce.model.ActivityType;
import com.alhakim.ecommerce.model.ProductReindex;
import com.alhakim.ecommerce.model.UserActivityReindex;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserActivityReindexConsumer {

    private final UserActivityService userActivityService;
    private final ProductService productService;

    @KafkaListener(topics = "${kafka.topic.user-activity-reindex.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(UserActivityReindex message) {
        if (!List.of(ActivityType.PURCHASE, ActivityType.VIEW).contains(message.getActivityType())) {
            return;
        }

        if (message.getProductId() == null || message.getUserId() == null) {
            return;
        }

        if (message.getActivityType().equals(ActivityType.PURCHASE)) {
            userActivityService.trackPurchase(message.getUserId(), message.getProductId());
            return;
        }

        if (message.getActivityType().equals(ActivityType.VIEW)) {
            userActivityService.trackProductView(message.getUserId(), message.getProductId());
        }
    }
}
