package com.casino.controller;

import com.casino.dto.KYCResponse;
import com.casino.dto.KYCReviewRequest;
import com.casino.entity.KYCDocument;
import com.casino.repository.AdminRepository;
import com.casino.service.KYCService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/kyc")
@RequiredArgsConstructor
public class AdminKYCController {

    private final KYCService kycService;
    private final AdminRepository adminRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_COMPLIANCE', 'ADMIN_SUPPORT')")
    public ResponseEntity<Page<KYCResponse>> getAllKYC(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) KYCDocument.KYCStatus status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<KYCDocument> kycDocs;

        if (status != null) {
            kycDocs = kycService.getKYCByStatus(status, pageable);
        } else {
            kycDocs = kycService.getAllKYC(pageable);
        }

        return ResponseEntity.ok(kycDocs.map(KYCResponse::fromEntity));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_COMPLIANCE', 'ADMIN_SUPPORT')")
    public ResponseEntity<KYCResponse> getKYCById(@PathVariable Long id) {
        KYCDocument kycDoc = kycService.getKYCById(id);
        return ResponseEntity.ok(KYCResponse.fromEntity(kycDoc));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_COMPLIANCE')")
    public ResponseEntity<KYCResponse> reviewKYC(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody KYCReviewRequest request) {

        String username = authentication.getName();
        Long adminId = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"))
                .getId();

        KYCDocument kycDoc = kycService.reviewKYC(id, adminId, request);
        return ResponseEntity.ok(KYCResponse.fromEntity(kycDoc));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_COMPLIANCE')")
    public ResponseEntity<KYCStats> getKYCStats() {
        KYCStats stats = new KYCStats();
        stats.setPending(kycService.countByStatus(KYCDocument.KYCStatus.PENDING));
        stats.setVerified(kycService.countByStatus(KYCDocument.KYCStatus.VERIFIED));
        stats.setRejected(kycService.countByStatus(KYCDocument.KYCStatus.REJECTED));
        stats.setTotal(stats.getPending() + stats.getVerified() + stats.getRejected());
        return ResponseEntity.ok(stats);
    }

    @Data
    public static class KYCStats {
        private long total;
        private long pending;
        private long verified;
        private long rejected;
    }
}
