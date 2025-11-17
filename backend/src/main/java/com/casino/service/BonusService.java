package com.casino.service;

import com.casino.entity.Bonus;
import com.casino.entity.User;
import com.casino.entity.UserBonus;
import com.casino.repository.BonusRepository;
import com.casino.repository.UserBonusRepository;
import com.casino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonusService {

    private final BonusRepository bonusRepository;
    private final UserBonusRepository userBonusRepository;
    private final UserRepository userRepository;

    public List<Bonus> getAllActiveBonuses() {
        return bonusRepository.findByStatus(Bonus.BonusStatus.ACTIVE);
    }

    public List<Bonus> getAllBonuses() {
        return bonusRepository.findAll();
    }

    public Bonus getBonusById(Long id) {
        return bonusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bonus not found"));
    }

    @Transactional
    public Bonus createBonus(Bonus bonus) {
        if (bonus.getUsedCount() == null) {
            bonus.setUsedCount(0);
        }
        return bonusRepository.save(bonus);
    }

    @Transactional
    public Bonus updateBonus(Long id, Bonus bonus) {
        Bonus existing = getBonusById(id);
        bonus.setId(id);
        bonus.setUsedCount(existing.getUsedCount());
        return bonusRepository.save(bonus);
    }

    @Transactional
    public void deleteBonus(Long id) {
        bonusRepository.deleteById(id);
    }

    public List<UserBonus> getUserBonuses(Long userId) {
        return userBonusRepository.findByUserId(userId);
    }

    public List<UserBonus> getActiveUserBonuses(Long userId) {
        return userBonusRepository.findByUserIdAndStatus(userId, UserBonus.UserBonusStatus.ACTIVE);
    }

    @Transactional
    public UserBonus claimBonus(Long userId, Long bonusId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bonus bonus = getBonusById(bonusId);

        // Validate bonus
        validateBonusClaim(user, bonus);

        // Check if user already claimed this bonus
        if (userBonusRepository.existsByUserIdAndBonusId(userId, bonusId)) {
            throw new RuntimeException("Bonus already claimed");
        }

        // Calculate bonus amount
        BigDecimal bonusAmount = calculateBonusAmount(bonus, user.getBalance());

        // Create user bonus
        UserBonus userBonus = new UserBonus();
        userBonus.setUser(user);
        userBonus.setBonus(bonus);
        userBonus.setBonusAmount(bonusAmount);
        userBonus.setClaimedAt(LocalDateTime.now());
        userBonus.setExpiresAt(bonus.getValidTo());
        userBonus.setStatus(UserBonus.UserBonusStatus.ACTIVE);

        // Calculate wagering requirement
        if (bonus.getWageringRequirement() != null && bonus.getWageringRequirement() > 0) {
            BigDecimal wageringRequired = bonusAmount.multiply(new BigDecimal(bonus.getWageringRequirement()));
            userBonus.setWageringRequired(wageringRequired);
            userBonus.setStatus(UserBonus.UserBonusStatus.WAGERING);
        }

        // Update bonus usage count
        bonus.setUsedCount(bonus.getUsedCount() + 1);
        bonusRepository.save(bonus);

        // Add bonus to user balance
        user.setBalance(user.getBalance().add(bonusAmount));
        userRepository.save(user);

        UserBonus saved = userBonusRepository.save(userBonus);
        log.info("User {} claimed bonus {} for amount {}", userId, bonusId, bonusAmount);

        return saved;
    }

    private void validateBonusClaim(User user, Bonus bonus) {
        // Check if bonus is active
        if (bonus.getStatus() != Bonus.BonusStatus.ACTIVE) {
            throw new RuntimeException("Bonus is not active");
        }

        // Check if bonus is valid
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(bonus.getValidFrom()) || now.isAfter(bonus.getValidTo())) {
            throw new RuntimeException("Bonus is not valid at this time");
        }

        // Check usage limit
        if (bonus.getUsageLimit() != null && bonus.getUsedCount() >= bonus.getUsageLimit()) {
            throw new RuntimeException("Bonus usage limit reached");
        }

        // Check minimum deposit for deposit bonuses
        if (bonus.getType() == Bonus.BonusType.DEPOSIT && bonus.getMinDeposit() != null) {
            if (user.getBalance().compareTo(bonus.getMinDeposit()) < 0) {
                throw new RuntimeException("Insufficient balance for this bonus");
            }
        }
    }

    private BigDecimal calculateBonusAmount(Bonus bonus, BigDecimal userBalance) {
        BigDecimal amount;

        if (bonus.getPercentage() != null && bonus.getPercentage().compareTo(BigDecimal.ZERO) > 0) {
            // Percentage bonus
            amount = userBalance.multiply(bonus.getPercentage()).divide(new BigDecimal(100));
        } else if (bonus.getAmount() != null) {
            // Fixed amount bonus
            amount = bonus.getAmount();
        } else {
            throw new RuntimeException("Bonus has no amount or percentage configured");
        }

        // Apply max bonus limit
        if (bonus.getMaxBonus() != null && amount.compareTo(bonus.getMaxBonus()) > 0) {
            amount = bonus.getMaxBonus();
        }

        return amount;
    }

    @Transactional
    public void updateWagering(Long userBonusId, BigDecimal wageringAmount) {
        UserBonus userBonus = userBonusRepository.findById(userBonusId)
                .orElseThrow(() -> new RuntimeException("User bonus not found"));

        if (userBonus.getStatus() != UserBonus.UserBonusStatus.WAGERING) {
            return;
        }

        userBonus.setWageringCompleted(userBonus.getWageringCompleted().add(wageringAmount));

        if (userBonus.isWageringComplete()) {
            userBonus.setStatus(UserBonus.UserBonusStatus.COMPLETED);
            userBonus.setCompletedAt(LocalDateTime.now());
            log.info("User bonus {} wagering completed", userBonusId);
        }

        userBonusRepository.save(userBonus);
    }

    @Transactional
    public void expireOldBonuses() {
        List<UserBonus> activeBonuses = userBonusRepository.findAll().stream()
                .filter(ub -> ub.getStatus() == UserBonus.UserBonusStatus.ACTIVE ||
                              ub.getStatus() == UserBonus.UserBonusStatus.WAGERING)
                .filter(ub -> ub.getExpiresAt() != null && ub.getExpiresAt().isBefore(LocalDateTime.now()))
                .toList();

        for (UserBonus userBonus : activeBonuses) {
            userBonus.setStatus(UserBonus.UserBonusStatus.EXPIRED);
            userBonusRepository.save(userBonus);
            log.info("Expired user bonus {}", userBonus.getId());
        }
    }
}
