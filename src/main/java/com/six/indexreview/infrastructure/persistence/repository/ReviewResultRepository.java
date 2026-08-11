package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewResultRepository extends JpaRepository<ReviewResultEntity, Long> {
    Optional<ReviewResultEntity> findById(Long id);

    Optional<ReviewResultEntity> findTopByIndexCodeAndReviewPeriodOrderByCreatedAtDescIdDesc(String indexCode,
                                                                                                String reviewPeriod);
}
