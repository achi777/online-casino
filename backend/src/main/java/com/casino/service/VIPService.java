package com.casino.service;

import com.casino.entity.*;
import com.casino.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VIPService {

    private final VIPTierRepository vipTierRepository;
    private final VIPPointsTransactionRepository vipPointsTransactionRepository;
    private final UserRepository userRepository;

    // Points calculation rates
    private static final BigDecimal POINTS_PER_GEL_WAGERED = new BigDecimal("1"); // 1 ქულა = 1 ლარი wagering
    private static final BigDecimal POINTS_PER_GEL_DEPOSIT = new BigDecimal("0.1"); // 0.1 ქულა = 1 ლარი deposit

    // VIP Tier Management
    public List<VIPTier> getAllTiers() {
        return vipTierRepository.findAllByOrderByLevelAsc();
    }

    public Optional<VIPTier> getTierById(Long id) {
        return vipTierRepository.findById(id);
    }

    @Transactional
    public VIPTier createTier(VIPTier tier) {
        return vipTierRepository.save(tier);
    }

    @Transactional
    public VIPTier updateTier(Long id, VIPTier tier) {
        VIPTier existing = vipTierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VIP Tier not found"));
        tier.setId(id);
        return vipTierRepository.save(tier);
    }

    @Transactional
    public void deleteTier(Long id) {
        vipTierRepository.deleteById(id);
    }

    // VIP Points Management
    @Transactional
    public void addPointsForWagering(Long userId, BigDecimal wageringAmount, GameSession gameSession) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Calculate points: 1 ლარი wagering = 1 VIP point
        int points = wageringAmount.multiply(POINTS_PER_GEL_WAGERED)
                .setScale(0, RoundingMode.DOWN)
                .intValue();

        if (points > 0) {
            addPoints(user, points, VIPPointsTransaction.TransactionType.WAGERING,
                    "Points earned from wagering ₾" + wageringAmount, wageringAmount, gameSession, null);

            // Update total wagered
            user.setTotalWagered(user.getTotalWagered().add(wageringAmount));
            userRepository.save(user);

            log.info("Added {} VIP points to user {} for wagering ₾{}", points, userId, wageringAmount);
        }
    }

    @Transactional
    public void addPointsForDeposit(Long userId, BigDecimal depositAmount, Transaction transaction) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Calculate points: 1 ლარი deposit = 0.1 VIP points
        int points = depositAmount.multiply(POINTS_PER_GEL_DEPOSIT)
                .setScale(0, RoundingMode.DOWN)
                .intValue();

        if (points > 0) {
            addPoints(user, points, VIPPointsTransaction.TransactionType.DEPOSIT,
                    "Points earned from deposit ₾" + depositAmount, depositAmount, null, transaction);

            // Update lifetime deposits
            user.setLifetimeDeposits(user.getLifetimeDeposits().add(depositAmount));
            userRepository.save(user);

            log.info("Added {} VIP points to user {} for deposit ₾{}", points, userId, depositAmount);
        }
    }

    @Transactional
    public void addManualPoints(Long userId, int points, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        addPoints(user, points, VIPPointsTransaction.TransactionType.MANUAL_ADJUSTMENT,
                description != null ? description : "Manual points adjustment by admin",
                null, null, null);

        log.info("Added {} VIP points manually to user {} by admin", points, userId);
    }

    @Transactional
    private void addPoints(User user, int points, VIPPointsTransaction.TransactionType type,
                          String description, BigDecimal relatedAmount,
                          GameSession gameSession, Transaction transaction) {
        // Update user points
        int oldPoints = user.getVipPoints();
        int newPoints = oldPoints + points;
        user.setVipPoints(newPoints);

        // Create transaction record
        VIPPointsTransaction vipTransaction = new VIPPointsTransaction();
        vipTransaction.setUser(user);
        vipTransaction.setPoints(points);
        vipTransaction.setType(type);
        vipTransaction.setDescription(description);
        vipTransaction.setRelatedAmount(relatedAmount);
        vipTransaction.setGameSession(gameSession);
        vipTransaction.setTransaction(transaction);
        vipPointsTransactionRepository.save(vipTransaction);

        // Check for level up
        checkAndProcessLevelUp(user, oldPoints, newPoints);

        userRepository.save(user);
    }

    @Transactional
    public void checkAndProcessLevelUp(User user, int oldPoints, int newPoints) {
        List<VIPTier> tiers = getAllTiers();
        VIPTier currentTier = user.getVipTier();
        VIPTier newTier = null;

        // Find appropriate tier for new points
        for (VIPTier tier : tiers) {
            if (newPoints >= tier.getMinPoints()) {
                newTier = tier;
            } else {
                break;
            }
        }

        // Check if leveled up
        if (newTier != null && (currentTier == null || newTier.getLevel() > currentTier.getLevel())) {
            user.setVipTier(newTier);

            // Award level up bonus points
            int levelUpBonus = newTier.getLevel() * 100; // 100 ქულა თითო ლეველზე
            VIPPointsTransaction bonusTransaction = new VIPPointsTransaction();
            bonusTransaction.setUser(user);
            bonusTransaction.setPoints(levelUpBonus);
            bonusTransaction.setType(VIPPointsTransaction.TransactionType.LEVEL_UP);
            bonusTransaction.setDescription("Congratulations! Level up to " + newTier.getName() + " tier. Bonus: " + levelUpBonus + " points");
            vipPointsTransactionRepository.save(bonusTransaction);

            user.setVipPoints(user.getVipPoints() + levelUpBonus);

            log.info("User {} leveled up to {} tier! Awarded {} bonus points",
                    user.getId(), newTier.getName(), levelUpBonus);
        }
    }

    // Get VIP Users
    public List<User> getVIPUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getVipTier() != null)
                .toList();
    }

    public List<User> getUsersByTier(Long tierId) {
        return userRepository.findAll().stream()
                .filter(user -> user.getVipTier() != null && user.getVipTier().getId().equals(tierId))
                .toList();
    }

    // Get User's VIP Points History
    public List<VIPPointsTransaction> getUserPointsHistory(Long userId) {
        return vipPointsTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<VIPPointsTransaction> getUserPointsHistory(Long userId, Pageable pageable) {
        return vipPointsTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<VIPPointsTransaction> getUserPointsHistoryByPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
        return vipPointsTransactionRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, start, end);
    }

    // Calculate cashback for user
    public BigDecimal calculateCashback(User user, BigDecimal lossAmount) {
        if (user.getVipTier() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal cashbackPercentage = user.getVipTier().getCashbackPercentage();
        if (cashbackPercentage == null || cashbackPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return lossAmount.multiply(cashbackPercentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    // Get bonus multiplier for user
    public BigDecimal getBonusMultiplier(User user) {
        if (user.getVipTier() == null) {
            return BigDecimal.ONE;
        }
        return user.getVipTier().getBonusMultiplier() != null
                ? user.getVipTier().getBonusMultiplier()
                : BigDecimal.ONE;
    }

    // Initialize default VIP tiers if they don't exist
    @Transactional
    public void initializeDefaultTiers() {
        if (vipTierRepository.count() == 0) {
            createDefaultTier("Bronze", 1, 0, "1.00", "1.00", "#CD7F32", 1);
            createDefaultTier("Silver", 2, 1000, "2.00", "1.20", "#C0C0C0", 2);
            createDefaultTier("Gold", 3, 5000, "3.00", "1.50", "#FFD700", 3);
            createDefaultTier("Platinum", 4, 10000, "5.00", "2.00", "#E5E4E2", 4);
            createDefaultTier("Diamond", 5, 50000, "8.00", "3.00", "#B9F2FF", 5);
            log.info("Default VIP tiers created");
        }
    }

    private void createDefaultTier(String name, int level, int minPoints, String cashback,
                                   String multiplier, String color, int sortOrder) {
        VIPTier tier = new VIPTier();
        tier.setName(name);
        tier.setLevel(level);
        tier.setMinPoints(minPoints);
        tier.setCashbackPercentage(new BigDecimal(cashback));
        tier.setBonusMultiplier(new BigDecimal(multiplier));
        tier.setColor(color);
        tier.setSortOrder(sortOrder);
        tier.setPrioritySupport(level);
        tier.setPersonalAccountManager(level >= 4);
        tier.setExclusivePromotions(level >= 3);
        vipTierRepository.save(tier);
    }
}
