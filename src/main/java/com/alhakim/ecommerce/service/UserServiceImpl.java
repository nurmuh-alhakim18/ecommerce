package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.common.errors.*;
import com.alhakim.ecommerce.entity.Role;
import com.alhakim.ecommerce.entity.User;
import com.alhakim.ecommerce.entity.UserRole;
import com.alhakim.ecommerce.model.UserRegisterRequest;
import com.alhakim.ecommerce.model.UserResponse;
import com.alhakim.ecommerce.model.UserUpdateRequest;
import com.alhakim.ecommerce.repository.RoleRepository;
import com.alhakim.ecommerce.repository.UserRepository;
import com.alhakim.ecommerce.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    private final CacheService cacheService;
    private final String USER_CACHE_KEY = "cache:user:";
    private final String USER_ROLE_CACHE_KEY = "cache:user:role:";

    @Override
    @Transactional
    public UserResponse register(UserRegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UsernameAlreadyExistsException("Email already exists");
        }

        if (!registerRequest.getPassword().equals(registerRequest.getPasswordConfirmation())) {
            throw new BadRequestException("Passwords do not match");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(encodedPassword)
                .enabled(true)
                .build();

        userRepository.save(user);

        Role userRole = roleRepository
                .findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        UserRole userRoleRelation = UserRole.builder()
                .id(new UserRole.UserRoleId(user.getUserId(), userRole.getRoleId()))
                .build();
        userRoleRepository.save(userRoleRelation);

        return UserResponse.fromUserAndRoles(user, List.of(userRole));
    }

    @Override
    public UserResponse findUserById(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        List<Role> roles = roleRepository.findByUserId(userId);
        return UserResponse.fromUserAndRoles(user, roles);
    }

    @Override
    public UserResponse findByUserByKeyword(String keyword) {
        User user = userRepository
                .findByKeyword(keyword)
                .orElseThrow(() -> new UserNotFoundException("User not found with username or email: " + keyword));

        List<Role> roles = roleRepository.findByUserId(user.getUserId());
        return UserResponse.fromUserAndRoles(user, roles);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (request.getCurrentPassword() != null && request.getNewPassword() != null) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new InvalidPasswordException("Passwords do not match");
            }

            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);
        }

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userExistsByUsername(request.getUsername())) {
                throw new UsernameAlreadyExistsException("Username already exists");
            }

            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userExistsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException("Email already exists");
            }

            user.setEmail(request.getEmail());
        }

        String userCacheKey = USER_CACHE_KEY + user.getUsername();
        String roleCacheKey = USER_ROLE_CACHE_KEY + user.getUsername();
        userRepository.save(user);
        List<Role> roles = roleRepository.findByUserId(user.getUserId());
        cacheService.evict(userCacheKey);
        cacheService.evict(roleCacheKey);
        return UserResponse.fromUserAndRoles(user, roles);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        userRoleRepository.deleteByIdUserId(user.getUserId());

        userRepository.delete(user);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
