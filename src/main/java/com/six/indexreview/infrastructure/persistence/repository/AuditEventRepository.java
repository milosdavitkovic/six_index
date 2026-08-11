package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Strategy contract for AuditEventRepository in the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {
    List<AuditEventEntity> findAllByReviewResult_IdAndSecurityIdOrderBySequenceNumberAsc(Long reviewResultId,
                                                                                            Integer securityId);
}
