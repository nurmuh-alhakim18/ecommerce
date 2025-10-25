package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.UserInfo;

public interface JwtService {
    String generateToken(UserInfo userInfo);

    boolean validateToken(String token);

    String getUsernameFromToken(String token);
}
