package com.casino.controller;

import com.casino.dto.AdminAuthRequest;
import com.casino.dto.AdminResponse;
import com.casino.dto.AuthResponse;
import com.casino.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}
