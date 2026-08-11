package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.domain.repository.AuditRepositoryPort;
import com.six.indexreview.infrastructure.persistence.entity.AuditEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter that exposes audit persistence through a technology-neutral port.
 *
 * Repository abstraction allows audit and review history persistence without
 * impacting methodology logic.
 */
@Component
@RequiredArgsConstructor
public class AuditRepositoryAdapter implements AuditRepositoryPort {
    private final AuditEventRepository delegate;

    @Override
    public List<AuditEventEntity> findAuditEventsForSecurity(Long reviewResultId, Integer securityId) {
        return delegate.findAllByReviewResult_IdAndSecurityIdOrderBySequenceNumberAsc(reviewResultId, securityId);
    }
}

