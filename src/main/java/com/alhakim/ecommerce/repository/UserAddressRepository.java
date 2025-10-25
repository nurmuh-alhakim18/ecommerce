package com.alhakim.ecommerce.repository;

import com.alhakim.ecommerce.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserId(Long userId);

    Optional<UserAddress> findByUserIdAndIsDefaultTrue(Long userId);

    @Query(value = """
        UPDATE user_address
        SET is_default = false
        WHERE user_id = :userId
        """, nativeQuery = true)
    void resetUserDefaultAddress(Long userId);

    @Query(value = """
        UPDATE user_address
        SET is_default = true
        WHERE user_address_id = :userAddressId
        """, nativeQuery = true)
    void setDefaultAddress(Long userAddressId);
}
