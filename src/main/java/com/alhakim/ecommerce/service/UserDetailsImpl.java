package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.entity.Role;
import com.alhakim.ecommerce.entity.User;
import com.alhakim.ecommerce.model.UserInfo;
import com.alhakim.ecommerce.repository.RoleRepository;
import com.alhakim.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetailsService {

    private final String USER_CACHE_KEY = "cache:user:";
    private final String USER_ROLE_CACHE_KEY = "cache:user:role:";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CacheService cacheService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String userCacheKey = USER_CACHE_KEY + username;
        String roleCacheKey = USER_ROLE_CACHE_KEY + username;

        Optional<User> userOpt = cacheService.get(userCacheKey, User.class);
        Optional<List<Role>> roleOpt = cacheService.get(roleCacheKey, new TypeReference<List<Role>>() {});

        if (userOpt.isPresent() && roleOpt.isPresent()) {
            return UserInfo.builder()
                    .roles(roleOpt.get())
                    .user(userOpt.get())
                    .build();
        }

        System.out.println(userCacheKey);
        System.out.println(userOpt.isPresent());
        User user = userRepository
                .findByKeyword(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<Role> roles = roleRepository.findByUserId(user.getUserId());

        UserInfo userInfo = UserInfo.builder().roles(roles).user(user).build();
        cacheService.put(userCacheKey, user);
        cacheService.put(roleCacheKey, roles);
        return userInfo;
    }
}
