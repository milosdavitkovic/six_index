package com.six.indexreview.domain.repository;

import com.six.indexreview.domain.model.AuditEvent;

import java.util.List;

/**
 * Repository abstraction for audit event history.
 *
 * Repository abstraction allows audit and review history persistence without
 * impacting methodology logic.
 * @author Milos Davitkovic
 */
public interface AuditRepositoryPort {
    List<AuditEvent> findAuditEventsForSecurity(Long reviewResultId, Integer securityId);
}

