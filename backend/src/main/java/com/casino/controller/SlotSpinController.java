package com.casino.controller;

import com.casino.dto.SpinRequest;
import com.casino.dto.SpinResponse;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.UserRepository;
import com.casino.service.SlotSpinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/games")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:8081"})
public class SlotSpinController {

    private final SlotSpinService slotSpinService;
    private final UserRepository userRepository;

    @PostMapping("/spin")
    public ResponseEntity<SpinResponse> spin(
            Authentication authentication,
            @RequestBody SpinRequest request) {
        Long userId = getUserIdFromAuth(authentication);
        SpinResponse response = slotSpinService.processSpin(userId, request);
        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null) {
            throw new BadRequestException("Authentication required");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found: " + email));
        return user.getId();
    }
}
