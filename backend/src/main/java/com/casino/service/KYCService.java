package com.casino.service;

import com.casino.dto.KYCReviewRequest;
import com.casino.dto.KYCSubmissionRequest;
import com.casino.entity.KYCDocument;
import com.casino.entity.User;
import com.casino.exception.BadRequestException;
import com.casino.repository.KYCDocumentRepository;
import com.casino.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KYCService {

    private final KYCDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;

    @Transactional
    public KYCDocument submitKYC(Long userId, KYCSubmissionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Check if user already has a pending or verified KYC
        Optional<KYCDocument> existing = kycDocumentRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
        if (existing.isPresent() && existing.get().getStatus() == KYCDocument.KYCStatus.PENDING) {
            throw new BadRequestException("You already have a pending KYC submission");
        }
        if (existing.isPresent() && existing.get().getStatus() == KYCDocument.KYCStatus.VERIFIED) {
            throw new BadRequestException("Your KYC is already verified");
        }

        // Create new KYC document
        KYCDocument kycDocument = new KYCDocument();
        kycDocument.setUser(user);
        kycDocument.setStatus(KYCDocument.KYCStatus.PENDING);
        kycDocument.setFirstName(request.getFirstName());
        kycDocument.setLastName(request.getLastName());
        kycDocument.setDateOfBirth(request.getDateOfBirth());
        kycDocument.setNationality(request.getNationality());
        kycDocument.setAddressLine1(request.getAddressLine1());
        kycDocument.setAddressLine2(request.getAddressLine2());
        kycDocument.setCity(request.getCity());
        kycDocument.setCountry(request.getCountry());
        kycDocument.setPostalCode(request.getPostalCode());
        kycDocument.setDocumentType(request.getDocumentType());
        kycDocument.setDocumentNumber(request.getDocumentNumber());
        kycDocument.setDocumentIssueDate(request.getDocumentIssueDate());
        kycDocument.setDocumentExpiryDate(request.getDocumentExpiryDate());
        kycDocument.setDocumentIssuingCountry(request.getDocumentIssuingCountry());
        kycDocument.setDocumentFrontImageUrl(request.getDocumentFrontImageUrl());
        kycDocument.setDocumentBackImageUrl(request.getDocumentBackImageUrl());
        kycDocument.setSelfieImageUrl(request.getSelfieImageUrl());
        kycDocument.setProofOfAddressUrl(request.getProofOfAddressUrl());
        kycDocument.setNotes(request.getNotes());
        kycDocument.setSubmittedAt(LocalDateTime.now());

        kycDocument = kycDocumentRepository.save(kycDocument);

        log.info("KYC submitted for user {}", userId);
        return kycDocument;
    }

    @Transactional(readOnly = true)
    public Optional<KYCDocument> getUserKYC(Long userId) {
        // First check if user has a VERIFIED KYC
        Optional<KYCDocument> verified = kycDocumentRepository.findFirstByUserIdAndStatus(userId, KYCDocument.KYCStatus.VERIFIED);
        if (verified.isPresent()) {
            return verified;
        }

        // If no verified KYC, return the most recent submission (PENDING or REJECTED)
        return kycDocumentRepository.findFirstByUserIdOrderBySubmittedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public KYCDocument getKYCById(Long kycId) {
        return kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new BadRequestException("KYC document not found"));
    }

    @Transactional(readOnly = true)
    public Page<KYCDocument> getAllKYC(Pageable pageable) {
        return kycDocumentRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<KYCDocument> getKYCByStatus(KYCDocument.KYCStatus status, Pageable pageable) {
        return kycDocumentRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public long countByStatus(KYCDocument.KYCStatus status) {
        return kycDocumentRepository.countByStatus(status);
    }

    @Transactional
    public KYCDocument reviewKYC(Long kycId, Long adminId, KYCReviewRequest request) {
        KYCDocument kycDocument = kycDocumentRepository.findById(kycId)
                .orElseThrow(() -> new BadRequestException("KYC document not found"));

        if (kycDocument.getStatus() != KYCDocument.KYCStatus.PENDING) {
            throw new BadRequestException("This KYC has already been reviewed");
        }

        // Validate rejection reason if status is REJECTED
        if (request.getStatus() == KYCDocument.KYCStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
                throw new BadRequestException("Rejection reason is required when rejecting KYC");
            }
        }

        kycDocument.setStatus(request.getStatus());
        kycDocument.setReviewedAt(LocalDateTime.now());
        kycDocument.setReviewedByAdminId(adminId);
        kycDocument.setRejectionReason(request.getRejectionReason());

        // Update user KYC status
        User user = kycDocument.getUser();
        if (request.getStatus() == KYCDocument.KYCStatus.VERIFIED) {
            user.setKycStatus(User.KYCStatus.VERIFIED);
            log.info("KYC verified for user {}", user.getId());
        } else if (request.getStatus() == KYCDocument.KYCStatus.REJECTED) {
            user.setKycStatus(User.KYCStatus.REJECTED);
            log.info("KYC rejected for user {} - Reason: {}", user.getId(), request.getRejectionReason());
        }
        userRepository.save(user);

        return kycDocumentRepository.save(kycDocument);
    }
}
