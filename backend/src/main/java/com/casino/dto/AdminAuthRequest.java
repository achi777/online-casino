package com.casino.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminAuthRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
