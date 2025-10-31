package com.casino.service;

import com.casino.entity.AuditLog;
import com.casino.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logUserAction(Long userId, String action, String entityType, Long entityId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setActorType(AuditLog.ActorType.USER);
        log.setActorId(userId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logAdminAction(Long adminId, String action, String entityType, Long entityId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setActorType(AuditLog.ActorType.ADMIN);
        log.setActorId(adminId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        auditLogRepository.save(log);
    }

    @Transactional
    public void logSystemAction(String action, String entityType, Long entityId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setActorType(AuditLog.ActorType.SYSTEM);
        log.setActorId(0L);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        auditLogRepository.save(log);
    }
}
