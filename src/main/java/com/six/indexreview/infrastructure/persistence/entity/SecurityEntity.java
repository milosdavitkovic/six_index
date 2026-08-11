package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Core SecurityEntity component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Getter
@Entity
@Table(name = "security")
public class SecurityEntity {
    @Id
    private Integer id;

    protected SecurityEntity() {
    }

    public SecurityEntity(Integer id) {
        this.id = id;
    }
}
