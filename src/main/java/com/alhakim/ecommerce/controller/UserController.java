package com.alhakim.ecommerce.controller;

import com.alhakim.ecommerce.common.errors.ForbiddenAccessException;
import com.alhakim.ecommerce.model.UserInfo;
import com.alhakim.ecommerce.model.UserResponse;
import com.alhakim.ecommerce.model.UserUpdateRequest;
import com.alhakim.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@SecurityRequirement(name = "Bearer")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        UserResponse userResponse = UserResponse.fromUserAndRoles(userInfo.getUser(), userInfo.getRoles());
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        if (userInfo.getUser().getUserId() != id && !userInfo.getAuthorities().contains("ROLE_ADMIN")) {
            throw new ForbiddenAccessException("Not allowed to update data");
        }

        UserResponse updatedUser = userService.updateUser(id, userUpdateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        if (userInfo.getUser().getUserId() != id && !userInfo.getAuthorities().contains("ROLE_ADMIN")) {
            throw new ForbiddenAccessException("Not allowed to update data");
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
