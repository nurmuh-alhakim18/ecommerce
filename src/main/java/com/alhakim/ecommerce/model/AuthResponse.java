package com.alhakim.ecommerce.model;

import com.alhakim.ecommerce.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private List<String> roles;

    public static AuthResponse fromUserInfo(UserInfo userInfo, String token) {
        return AuthResponse.builder()
                .token(token)
                .userId(userInfo.getUser().getUserId())
                .username(userInfo.getUsername())
                .email(userInfo.getUser().getEmail())
                .roles(userInfo.getRoles().stream().map(Role::getName).toList())
                .build();
    }
}
