package com.six.indexreview.infrastructure.persistence.mapper;

import com.six.indexreview.domain.model.AuditEvent;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditEventMapper {
    public AuditEvent toDomain(AuditEventEntity entity) {
        return new AuditEvent(entity.getTimestamp(), entity.getRuleCode(),
                entity.getSecurityId() == null ? null : new SecurityId(entity.getSecurityId()),
                entity.getMessage(), entity.getInputValue(), entity.getOutputValue());
    }
}
