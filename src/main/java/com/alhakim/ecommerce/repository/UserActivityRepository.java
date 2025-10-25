package com.alhakim.ecommerce.repository;

import com.alhakim.ecommerce.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    @Query(value = """
        SELECT COUNT(*)
        FROM user_activity
        WHERE product_id = :productId AND activity_type = :activityType
        """, nativeQuery = true)
    Long countProductActivityByType(Long productId, String activityType);

    @Query(value = """
        SELECT COUNT(*)
        FROM user_activity
        WHERE product_id = :productId AND activity_type = :activityType AND created_at BETWEEN :start AND :end
        """, nativeQuery = true)
    Long countProductActivityByTypeAndDateRange(Long productId, String activityType, LocalDateTime start, LocalDateTime end);

    @Query(value = """
        SELECT *
        FROM user_activity
        WHERE user_id = :userId AND activity_type = :activityType AND created_at BETWEEN :start AND :end
        """, nativeQuery = true)
    List<UserActivity> findByUserIdAndType(Long userId, String activityType, LocalDateTime start, LocalDateTime end);
}
