package com.six.indexreview.domain.repository;

import com.six.indexreview.infrastructure.persistence.entity.AuditEventEntity;

import java.util.List;

/**
 * Repository abstraction for audit event history.
 *
 * Repository abstraction allows audit and review history persistence without
 * impacting methodology logic.
 */
public interface AuditRepositoryPort {
    List<AuditEventEntity> findAuditEventsForSecurity(Long reviewResultId, Integer securityId);
}

