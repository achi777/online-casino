package com.casino.repository;

import com.casino.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByCode(String code);
    List<PaymentMethod> findByEnabledTrueOrderBySortOrderAsc();
    List<PaymentMethod> findByTypeAndEnabledTrue(PaymentMethod.PaymentType type);
}
