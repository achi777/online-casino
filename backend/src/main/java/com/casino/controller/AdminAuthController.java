package com.casino.controller;

import com.casino.dto.*;
import com.casino.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AdminAuthRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AdminResponse> getCurrentAdmin(Authentication authentication) {
        return ResponseEntity.ok(adminService.getCurrentAdminProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<AdminResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody AdminUpdateProfileRequest request) {
        return ResponseEntity.ok(adminService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        adminService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
