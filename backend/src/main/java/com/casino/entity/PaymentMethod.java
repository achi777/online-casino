package com.casino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_methods")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal minAmount;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal maxAmount;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal feePercentage = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal fixedFee = BigDecimal.ZERO;

    private String processingTime;

    private String logoUrl;

    private Boolean supportsDeposit = true;

    private Boolean supportsWithdrawal = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public enum PaymentType {
        CARD,
        BANK_TRANSFER,
        EWALLET,
        CRYPTOCURRENCY,
        MOBILE_PAYMENT,
        VOUCHER
    }
}
