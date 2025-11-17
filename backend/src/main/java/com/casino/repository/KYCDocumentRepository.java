package com.casino.repository;

import com.casino.entity.KYCDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KYCDocumentRepository extends JpaRepository<KYCDocument, Long> {
    Optional<KYCDocument> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<KYCDocument> findFirstByUserIdOrderBySubmittedAtDesc(Long userId);
    Optional<KYCDocument> findFirstByUserIdAndStatus(Long userId, KYCDocument.KYCStatus status);
    List<KYCDocument> findByUserId(Long userId);
    List<KYCDocument> findByStatus(KYCDocument.KYCStatus status);
    Page<KYCDocument> findByStatus(KYCDocument.KYCStatus status, Pageable pageable);
    Page<KYCDocument> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatus(KYCDocument.KYCStatus status);
}
