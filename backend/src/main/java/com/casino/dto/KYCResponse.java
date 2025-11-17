package com.casino.dto;

import com.casino.entity.KYCDocument;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class KYCResponse {
    private Long id;
    private Long userId;
    private KYCDocument.KYCStatus status;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String nationality;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String country;
    private String postalCode;
    private KYCDocument.DocumentType documentType;
    private String documentNumber;
    private LocalDate documentIssueDate;
    private LocalDate documentExpiryDate;
    private String documentIssuingCountry;
    private String documentFrontImageUrl;
    private String documentBackImageUrl;
    private String selfieImageUrl;
    private String proofOfAddressUrl;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedByAdminId;
    private String rejectionReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KYCResponse fromEntity(KYCDocument doc) {
        if (doc == null) return null;

        KYCResponse response = new KYCResponse();
        response.setId(doc.getId());
        response.setUserId(doc.getUser().getId());
        response.setStatus(doc.getStatus());
        response.setFirstName(doc.getFirstName());
        response.setLastName(doc.getLastName());
        response.setDateOfBirth(doc.getDateOfBirth());
        response.setNationality(doc.getNationality());
        response.setAddressLine1(doc.getAddressLine1());
        response.setAddressLine2(doc.getAddressLine2());
        response.setCity(doc.getCity());
        response.setCountry(doc.getCountry());
        response.setPostalCode(doc.getPostalCode());
        response.setDocumentType(doc.getDocumentType());
        response.setDocumentNumber(doc.getDocumentNumber());
        response.setDocumentIssueDate(doc.getDocumentIssueDate());
        response.setDocumentExpiryDate(doc.getDocumentExpiryDate());
        response.setDocumentIssuingCountry(doc.getDocumentIssuingCountry());
        response.setDocumentFrontImageUrl(doc.getDocumentFrontImageUrl());
        response.setDocumentBackImageUrl(doc.getDocumentBackImageUrl());
        response.setSelfieImageUrl(doc.getSelfieImageUrl());
        response.setProofOfAddressUrl(doc.getProofOfAddressUrl());
        response.setSubmittedAt(doc.getSubmittedAt());
        response.setReviewedAt(doc.getReviewedAt());
        response.setReviewedByAdminId(doc.getReviewedByAdminId());
        response.setRejectionReason(doc.getRejectionReason());
        response.setNotes(doc.getNotes());
        response.setCreatedAt(doc.getCreatedAt());
        response.setUpdatedAt(doc.getUpdatedAt());
        return response;
    }
}
