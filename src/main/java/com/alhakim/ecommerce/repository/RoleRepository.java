package com.alhakim.ecommerce.repository;

import com.alhakim.ecommerce.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query(value = """
        SELECT r.*
        FROM roles AS r
        JOIN user_role AS ur ON r.role_id = ur.role_id
        JOIN users AS u ON ur.user_id = u.user_id
        WHERE u.user_id = :userId
        """, nativeQuery = true)
    List<Role> findByUserId(Long userId);
}
