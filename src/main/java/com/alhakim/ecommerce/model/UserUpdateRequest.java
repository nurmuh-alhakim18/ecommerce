package com.alhakim.ecommerce.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "Username must be in between 3-50 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be in between 8-100 characters")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String currentPassword;
}
