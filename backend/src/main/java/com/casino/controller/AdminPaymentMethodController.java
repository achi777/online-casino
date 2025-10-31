package com.casino.controller;

import com.casino.entity.PaymentMethod;
import com.casino.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payment-methods")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_FINANCE')")
public class AdminPaymentMethodController {

    private final PaymentMethodRepository paymentMethodRepository;

    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getAllPaymentMethods() {
        return ResponseEntity.ok(paymentMethodRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> getPaymentMethodById(@PathVariable Long id) {
        return paymentMethodRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE')")
    public ResponseEntity<PaymentMethod> createPaymentMethod(@RequestBody PaymentMethod paymentMethod) {
        PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE')")
    public ResponseEntity<PaymentMethod> updatePaymentMethod(@PathVariable Long id, @RequestBody PaymentMethod paymentMethod) {
        return paymentMethodRepository.findById(id)
                .map(existing -> {
                    paymentMethod.setId(id);
                    return ResponseEntity.ok(paymentMethodRepository.save(paymentMethod));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_FINANCE')")
    public ResponseEntity<PaymentMethod> togglePaymentMethod(@PathVariable Long id) {
        return paymentMethodRepository.findById(id)
                .map(method -> {
                    method.setEnabled(!method.getEnabled());
                    return ResponseEntity.ok(paymentMethodRepository.save(method));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
