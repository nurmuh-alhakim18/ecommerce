package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.UserRegisterRequest;
import com.alhakim.ecommerce.model.UserResponse;
import com.alhakim.ecommerce.model.UserUpdateRequest;

public interface UserService {
    UserResponse register(UserRegisterRequest registerRequest);
    UserResponse findUserById(Long userId);
    UserResponse findByUserByKeyword(String keyword);
    UserResponse updateUser(Long userId, UserUpdateRequest request);
    void deleteUser(Long userId);
    boolean userExistsByUsername(String username);
    boolean userExistsByEmail(String email);
}
