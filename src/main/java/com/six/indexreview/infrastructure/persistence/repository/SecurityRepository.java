package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.SecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Strategy contract for SecurityRepository in the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
public interface SecurityRepository extends JpaRepository<SecurityEntity, Integer> {
}
