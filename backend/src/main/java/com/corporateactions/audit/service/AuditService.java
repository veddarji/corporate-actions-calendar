package com.corporateactions.audit.service;

import com.corporateactions.audit.entity.AuditLog;
import com.corporateactions.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async("taskExecutor")
    public void log(Long userId, String action, String resourceType,
                    String resourceId, boolean success, String errorMessage) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .success(success)
                    .errorMessage(errorMessage)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void log(String action, String resourceType, String resourceId) {
        log(null, action, resourceType, resourceId, true, null);
    }
}
