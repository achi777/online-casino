package com.casino.controller;

import com.casino.dto.KYCResponse;
import com.casino.dto.KYCSubmissionRequest;
import com.casino.entity.KYCDocument;
import com.casino.repository.UserRepository;
import com.casino.service.KYCService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/kyc")
@RequiredArgsConstructor
public class UserKYCController {

    private final KYCService kycService;
    private final UserRepository userRepository;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<KYCResponse> submitKYC(
            Authentication authentication,
            @Valid @RequestBody KYCSubmissionRequest request) {

        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        KYCDocument kycDocument = kycService.submitKYC(userId, request);
        return ResponseEntity.ok(KYCResponse.fromEntity(kycDocument));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<KYCResponse> getKYCStatus(Authentication authentication) {
        String email = authentication.getName();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        return kycService.getUserKYC(userId)
                .map(KYCResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
