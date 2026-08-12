package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.domain.model.AuditEvent;
import com.six.indexreview.domain.repository.AuditRepositoryPort;
import com.six.indexreview.infrastructure.persistence.mapper.AuditEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter that exposes audit persistence through a technology-neutral port.
 *
 * Repository abstraction allows audit and review history persistence without
 * impacting methodology logic.
 */
/**
 * @author Milos Davitkovic
 */
@Component
@RequiredArgsConstructor
public class AuditRepositoryAdapter implements AuditRepositoryPort {
    private final AuditEventRepository delegate;
    private final AuditEventMapper auditEventMapper;

    @Override
    public List<AuditEvent> findAuditEventsForSecurity(Long reviewResultId, Integer securityId) {
        return delegate.findAllByReviewResult_IdAndSecurityIdOrderBySequenceNumberAsc(reviewResultId, securityId)
                .stream()
                .map(auditEventMapper::toDomain)
                .toList();
    }
}

