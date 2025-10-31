package com.casino.controller;

import com.casino.entity.Banner;
import com.casino.entity.SystemNotification;
import com.casino.repository.BannerRepository;
import com.casino.repository.SystemNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_CONTENT')")
public class AdminCMSController {

    private final BannerRepository bannerRepository;
    private final SystemNotificationRepository notificationRepository;

    // Banners
    @GetMapping("/banners")
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerRepository.findAll());
    }

    @PostMapping("/banners")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_CONTENT')")
    public ResponseEntity<Banner> createBanner(@RequestBody Banner banner) {
        Banner saved = bannerRepository.save(banner);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/banners/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_CONTENT')")
    public ResponseEntity<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        return bannerRepository.findById(id)
                .map(existing -> {
                    banner.setId(id);
                    return ResponseEntity.ok(bannerRepository.save(banner));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/banners/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN')")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Notifications
    @GetMapping("/notifications")
    public ResponseEntity<List<SystemNotification>> getAllNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }

    @PostMapping("/notifications")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_CONTENT')")
    public ResponseEntity<SystemNotification> createNotification(@RequestBody SystemNotification notification) {
        SystemNotification saved = notificationRepository.save(notification);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/notifications/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN', 'ADMIN_CONTENT')")
    public ResponseEntity<SystemNotification> updateNotification(@PathVariable Long id, @RequestBody SystemNotification notification) {
        return notificationRepository.findById(id)
                .map(existing -> {
                    notification.setId(id);
                    return ResponseEntity.ok(notificationRepository.save(notification));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/notifications/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_OWNER', 'ADMIN_ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
