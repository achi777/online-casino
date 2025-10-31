package com.casino.service;

import com.casino.dto.ChangePasswordRequest;
import com.casino.dto.GameSessionResponse;
import com.casino.dto.UpdateProfileRequest;
import com.casino.dto.UserResponse;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.GameSessionRepository;
import com.casino.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Check if phone already exists for another user
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Phone number already exists");
            }
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);

        auditService.logUserAction(userId, "PROFILE_UPDATED", "User", userId, null,
                "User profile updated");

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.logUserAction(userId, "PASSWORD_CHANGED", "User", userId, null,
                "User password changed");
    }

    public Page<GameSessionResponse> getGameHistory(Long userId, Pageable pageable) {
        return gameSessionRepository.findByUserId(userId, pageable)
                .map(GameSessionResponse::fromEntity);
    }
}
