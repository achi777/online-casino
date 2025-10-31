package com.casino.controller;

import com.casino.dto.AdminResponse;
import com.casino.dto.CreateAdminRequest;
import com.casino.dto.DashboardStatsResponse;
import com.casino.entity.Admin;
import com.casino.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/management")
@RequiredArgsConstructor
public class AdminManagementController {

    private final AdminService adminService;

    @PostMapping("/admins")
    public ResponseEntity<AdminResponse> createAdmin(
            Authentication authentication,
            @Valid @RequestBody CreateAdminRequest request) {
        Long adminId = 1L; // Placeholder
        return ResponseEntity.ok(adminService.createAdmin(adminId, request));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/admins")
    public ResponseEntity<Page<AdminResponse>> getAllAdmins(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllAdmins(pageable));
    }

    @PutMapping("/admins/{adminId}/status")
    public ResponseEntity<AdminResponse> updateAdminStatus(
            Authentication authentication,
            @PathVariable Long adminId,
            @RequestBody Map<String, String> request) {
        Long currentAdminId = 1L; // Placeholder
        Admin.AdminStatus status = Admin.AdminStatus.valueOf(request.get("status"));
        return ResponseEntity.ok(adminService.updateAdminStatus(currentAdminId, adminId, status));
    }
}
