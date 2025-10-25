package com.alhakim.ecommerce.repository;

import com.alhakim.ecommerce.entity.UserRole;
import com.alhakim.ecommerce.entity.UserRole.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    void deleteByIdUserId(Long userId);
}
