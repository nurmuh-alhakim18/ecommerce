package com.alhakim.ecommerce.controller;

import com.alhakim.ecommerce.entity.User;
import com.alhakim.ecommerce.model.*;
import com.alhakim.ecommerce.service.AuthService;
import com.alhakim.ecommerce.service.JwtService;
import com.alhakim.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest authRequest) {
        UserInfo userInfo = authService.authenticate(authRequest);
        String token = jwtService.generateToken(userInfo);
        AuthResponse authResponse = AuthResponse.fromUserInfo(userInfo, token);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        UserResponse userResponse = userService.register(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
