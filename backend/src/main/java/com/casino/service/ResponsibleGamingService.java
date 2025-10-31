package com.casino.service;

import com.casino.dto.ResponsibleGamingRequest;
import com.casino.dto.ResponsibleGamingResponse;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResponsibleGamingService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public ResponsibleGamingResponse setLimits(Long userId, ResponsibleGamingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String oldValue = buildLimitsString(user);

        // Update limits
        if (request.getDailyDepositLimit() != null) {
            user.setDailyDepositLimit(request.getDailyDepositLimit());
        }
        if (request.getWeeklyDepositLimit() != null) {
            user.setWeeklyDepositLimit(request.getWeeklyDepositLimit());
        }
        if (request.getMonthlyDepositLimit() != null) {
            user.setMonthlyDepositLimit(request.getMonthlyDepositLimit());
        }
        if (request.getDailyTimeLimit() != null) {
            user.setDailyTimeLimit(request.getDailyTimeLimit());
        }

        user = userRepository.save(user);

        String newValue = buildLimitsString(user);
        auditService.logUserAction(userId, "LIMITS_UPDATED", "User", userId, oldValue, newValue);

        return ResponsibleGamingResponse.fromUser(user);
    }

    @Transactional
    public ResponsibleGamingResponse setSelfExclusion(Long userId, LocalDateTime until) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (until.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Self-exclusion date must be in the future");
        }

        LocalDateTime oldValue = user.getSelfExclusionUntil();
        user.setSelfExclusionUntil(until);
        user.setStatus(User.UserStatus.SUSPENDED);
        user = userRepository.save(user);

        auditService.logUserAction(userId, "SELF_EXCLUSION_SET", "User", userId,
                oldValue != null ? oldValue.toString() : null, until.toString());

        return ResponsibleGamingResponse.fromUser(user);
    }

    @Transactional
    public ResponsibleGamingResponse removeSelfExclusion(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getSelfExclusionUntil() == null) {
            throw new BadRequestException("No active self-exclusion");
        }

        if (user.getSelfExclusionUntil().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Self-exclusion period has not ended yet");
        }

        user.setSelfExclusionUntil(null);
        user.setStatus(User.UserStatus.ACTIVE);
        user = userRepository.save(user);

        auditService.logUserAction(userId, "SELF_EXCLUSION_REMOVED", "User", userId,
                "Self-exclusion removed", null);

        return ResponsibleGamingResponse.fromUser(user);
    }

    @Transactional
    public ResponsibleGamingResponse setTemporaryBlock(Long userId, Boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setTemporaryBlock(blocked);
        user = userRepository.save(user);

        auditService.logUserAction(userId, "TEMPORARY_BLOCK_CHANGED", "User", userId,
                String.valueOf(!blocked), String.valueOf(blocked));

        return ResponsibleGamingResponse.fromUser(user);
    }

    public ResponsibleGamingResponse getLimits(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return ResponsibleGamingResponse.fromUser(user);
    }

    private String buildLimitsString(User user) {
        return String.format("Daily: %s, Weekly: %s, Monthly: %s, Time: %s",
                user.getDailyDepositLimit(),
                user.getWeeklyDepositLimit(),
                user.getMonthlyDepositLimit(),
                user.getDailyTimeLimit());
    }
}
