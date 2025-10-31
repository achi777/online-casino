package com.casino.service;

import com.casino.dto.*;
import com.casino.entity.Admin;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.*;
import com.casino.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final GameRepository gameRepository;
    private final GameSessionRepository gameSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthResponse login(AdminAuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = jwtUtil.generateAccessToken(authentication.getName());
        String refreshToken = jwtUtil.generateRefreshToken(authentication.getName());

        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Admin not found"));

        admin.setLastLoginAt(java.time.LocalDateTime.now());
        adminRepository.save(admin);

        auditService.logAdminAction(admin.getId(), "ADMIN_LOGIN", "Admin", admin.getId(),
                null, "Admin logged in");

        return new AuthResponse(accessToken, refreshToken);
    }

    public AdminResponse getCurrentAdminProfile(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Admin not found"));
        return AdminResponse.fromEntity(admin);
    }

    @Transactional
    public AdminResponse createAdmin(Long creatorId, CreateAdminRequest request) {
        // Check if creator has OWNER role
        Admin creator = adminRepository.findById(creatorId)
                .orElseThrow(() -> new BadRequestException("Creator not found"));

        if (creator.getRole() != Admin.AdminRole.OWNER) {
            throw new BadRequestException("Only OWNER can create admins");
        }

        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setRole(request.getRole());
        admin = adminRepository.save(admin);

        auditService.logAdminAction(creatorId, "ADMIN_CREATED", "Admin", admin.getId(),
                null, String.format("Admin %s created with role %s", admin.getUsername(), admin.getRole()));

        return AdminResponse.fromEntity(admin);
    }

    public Page<AdminResponse> getAllAdmins(Pageable pageable) {
        return adminRepository.findAll(pageable)
                .map(AdminResponse::fromEntity);
    }

    @Transactional
    public AdminResponse updateAdminStatus(Long adminId, Long targetAdminId, Admin.AdminStatus status) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BadRequestException("Admin not found"));

        if (admin.getRole() != Admin.AdminRole.OWNER) {
            throw new BadRequestException("Only OWNER can change admin status");
        }

        Admin targetAdmin = adminRepository.findById(targetAdminId)
                .orElseThrow(() -> new BadRequestException("Target admin not found"));

        String oldStatus = targetAdmin.getStatus().name();
        targetAdmin.setStatus(status);
        targetAdmin = adminRepository.save(targetAdmin);

        auditService.logAdminAction(adminId, "ADMIN_STATUS_CHANGED", "Admin", targetAdminId,
                oldStatus, status.name());

        return AdminResponse.fromEntity(targetAdmin);
    }

    // User Management Methods
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    @Transactional
    public UserResponse updateUserStatus(Long adminId, Long userId, User.UserStatus status) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BadRequestException("Admin not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String oldStatus = user.getStatus().name();
        user.setStatus(status);
        user = userRepository.save(user);

        auditService.logAdminAction(adminId, "USER_STATUS_CHANGED", "User", userId,
                oldStatus, status.name());

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateKYCStatus(Long adminId, Long userId, User.KYCStatus kycStatus) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BadRequestException("Admin not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String oldStatus = user.getKycStatus().name();
        user.setKycStatus(kycStatus);
        user = userRepository.save(user);

        auditService.logAdminAction(adminId, "USER_KYC_CHANGED", "User", userId,
                oldStatus, kycStatus.name());

        return UserResponse.fromEntity(user);
    }

    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // User statistics
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByStatus(User.UserStatus.ACTIVE));
        stats.setPendingKYC(userRepository.countByKycStatus(User.KYCStatus.PENDING));

        // Game statistics
        stats.setTotalGames(gameRepository.count());
        stats.setActiveGames(gameRepository.countByStatus(com.casino.entity.Game.GameStatus.ACTIVE));

        // Financial statistics - today
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().plusDays(1).atStartOfDay();

        stats.setTotalDepositsToday(transactionRepository
                .sumAmountByTypeAndCreatedAtBetween("DEPOSIT", startOfToday, endOfToday)
                .orElse(BigDecimal.ZERO));

        stats.setTotalWithdrawalsToday(transactionRepository
                .sumAmountByTypeAndCreatedAtBetween("WITHDRAWAL", startOfToday, endOfToday)
                .orElse(BigDecimal.ZERO));

        stats.setTotalBetsToday(gameSessionRepository
                .sumTotalBetByCreatedAtBetween(startOfToday, endOfToday)
                .orElse(BigDecimal.ZERO));

        BigDecimal totalWinsToday = gameSessionRepository
                .sumTotalWinByCreatedAtBetween(startOfToday, endOfToday)
                .orElse(BigDecimal.ZERO);

        stats.setGgrToday(stats.getTotalBetsToday().subtract(totalWinsToday));

        // Financial statistics - this month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();

        stats.setTotalDepositsMonth(transactionRepository
                .sumAmountByTypeAndCreatedAtBetween("DEPOSIT", startOfMonth, endOfMonth)
                .orElse(BigDecimal.ZERO));

        stats.setTotalWithdrawalsMonth(transactionRepository
                .sumAmountByTypeAndCreatedAtBetween("WITHDRAWAL", startOfMonth, endOfMonth)
                .orElse(BigDecimal.ZERO));

        stats.setTotalBetsMonth(gameSessionRepository
                .sumTotalBetByCreatedAtBetween(startOfMonth, endOfMonth)
                .orElse(BigDecimal.ZERO));

        BigDecimal totalWinsMonth = gameSessionRepository
                .sumTotalWinByCreatedAtBetween(startOfMonth, endOfMonth)
                .orElse(BigDecimal.ZERO);

        stats.setGgrMonth(stats.getTotalBetsMonth().subtract(totalWinsMonth));

        // Pending withdrawals
        stats.setPendingWithdrawals(transactionRepository
                .countByTypeAndStatus("WITHDRAWAL", com.casino.entity.Transaction.TransactionStatus.PENDING));

        return stats;
    }
}
