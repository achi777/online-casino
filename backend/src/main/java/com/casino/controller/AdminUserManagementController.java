package com.casino.controller;

import com.casino.dto.UserResponse;
import com.casino.entity.User;
import com.casino.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        Long adminId = 1L; // Placeholder
        User.UserStatus status = User.UserStatus.valueOf(request.get("status"));
        return ResponseEntity.ok(adminService.updateUserStatus(adminId, userId, status));
    }

    @PutMapping("/{userId}/kyc")
    public ResponseEntity<UserResponse> updateKYCStatus(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        Long adminId = 1L; // Placeholder
        User.KYCStatus kycStatus = User.KYCStatus.valueOf(request.get("kycStatus"));
        return ResponseEntity.ok(adminService.updateKYCStatus(adminId, userId, kycStatus));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            Authentication authentication,
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        Long adminId = 1L; // Placeholder

        UserResponse response = null;
        if (request.containsKey("status")) {
            User.UserStatus status = User.UserStatus.valueOf(request.get("status"));
            response = adminService.updateUserStatus(adminId, userId, status);
        }
        if (request.containsKey("kycStatus")) {
            User.KYCStatus kycStatus = User.KYCStatus.valueOf(request.get("kycStatus"));
            response = adminService.updateKYCStatus(adminId, userId, kycStatus);
        }

        return ResponseEntity.ok(response);
    }
}
