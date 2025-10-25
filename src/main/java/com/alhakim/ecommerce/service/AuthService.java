package com.alhakim.ecommerce.service;

import com.alhakim.ecommerce.model.AuthRequest;
import com.alhakim.ecommerce.model.UserInfo;

public interface AuthService {
    UserInfo authenticate(AuthRequest authRequest);
}
