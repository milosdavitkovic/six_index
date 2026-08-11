package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {
    List<AuditEventEntity> findAllByReviewResult_IdAndSecurityIdOrderBySequenceNumberAsc(Long reviewResultId,
                                                                                            Integer securityId);
}
