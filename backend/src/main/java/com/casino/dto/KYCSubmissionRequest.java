package com.casino.dto;

import com.casino.entity.KYCDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class KYCSubmissionRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    @NotBlank(message = "Address is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @NotNull(message = "Document type is required")
    private KYCDocument.DocumentType documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotNull(message = "Document issue date is required")
    private LocalDate documentIssueDate;

    @NotNull(message = "Document expiry date is required")
    private LocalDate documentExpiryDate;

    private String documentIssuingCountry;

    // File paths/URLs - these will be set after file upload
    private String documentFrontImageUrl;
    private String documentBackImageUrl;
    private String selfieImageUrl;
    private String proofOfAddressUrl;

    private String notes;
}
