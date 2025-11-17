package com.casino.dto;

import com.casino.entity.KYCDocument;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KYCReviewRequest {

    @NotNull(message = "Status is required")
    private KYCDocument.KYCStatus status;  // VERIFIED or REJECTED

    private String rejectionReason;  // Required if status is REJECTED
}
