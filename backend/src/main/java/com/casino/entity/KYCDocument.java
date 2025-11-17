package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_documents")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class KYCDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KYCStatus status = KYCStatus.PENDING;

    // Personal Information
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private String nationality;

    // Address
    @Column(nullable = false)
    private String addressLine1;

    private String addressLine2;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String postalCode;

    // Identity Document
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String documentNumber;

    @Column(nullable = false)
    private LocalDate documentIssueDate;

    @Column(nullable = false)
    private LocalDate documentExpiryDate;

    private String documentIssuingCountry;

    // Document Files
    private String documentFrontImageUrl;  // ID/Passport front
    private String documentBackImageUrl;   // ID back (if applicable)
    private String selfieImageUrl;         // Selfie with ID
    private String proofOfAddressUrl;      // Utility bill, bank statement

    // Review Information
    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by_admin_id")
    private Long reviewedByAdminId;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // Additional Info
    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum KYCStatus {
        PENDING,      // Submitted, waiting for review
        VERIFIED,     // Approved
        REJECTED      // Rejected with reason
    }

    public enum DocumentType {
        PASSPORT,
        NATIONAL_ID,
        DRIVERS_LICENSE
    }
}
