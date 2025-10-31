package com.casino.controller;

import com.casino.dto.ResponsibleGamingRequest;
import com.casino.dto.ResponsibleGamingResponse;
import com.casino.service.ResponsibleGamingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/user/responsible-gaming")
@RequiredArgsConstructor
public class ResponsibleGamingController {

    private final ResponsibleGamingService responsibleGamingService;

    @GetMapping("/limits")
    public ResponseEntity<ResponsibleGamingResponse> getLimits(Authentication authentication) {
        Long userId = 1L; // Placeholder
        return ResponseEntity.ok(responsibleGamingService.getLimits(userId));
    }

    @PutMapping("/limits")
    public ResponseEntity<ResponsibleGamingResponse> setLimits(
            Authentication authentication,
            @Valid @RequestBody ResponsibleGamingRequest request) {
        Long userId = 1L; // Placeholder
        return ResponseEntity.ok(responsibleGamingService.setLimits(userId, request));
    }

    @PostMapping("/self-exclusion")
    public ResponseEntity<ResponsibleGamingResponse> setSelfExclusion(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        Long userId = 1L; // Placeholder
        LocalDateTime until = LocalDateTime.parse(request.get("until"));
        return ResponseEntity.ok(responsibleGamingService.setSelfExclusion(userId, until));
    }

    @DeleteMapping("/self-exclusion")
    public ResponseEntity<ResponsibleGamingResponse> removeSelfExclusion(Authentication authentication) {
        Long userId = 1L; // Placeholder
        return ResponseEntity.ok(responsibleGamingService.removeSelfExclusion(userId));
    }

    @PutMapping("/temporary-block")
    public ResponseEntity<ResponsibleGamingResponse> setTemporaryBlock(
            Authentication authentication,
            @RequestBody Map<String, Boolean> request) {
        Long userId = 1L; // Placeholder
        Boolean blocked = request.get("blocked");
        return ResponseEntity.ok(responsibleGamingService.setTemporaryBlock(userId, blocked));
    }
}
