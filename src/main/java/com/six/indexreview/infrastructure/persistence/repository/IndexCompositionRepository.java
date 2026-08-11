package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.IndexCompositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexCompositionRepository extends JpaRepository<IndexCompositionEntity, Long> {
    List<IndexCompositionEntity> findAllByIndexCodeAndReviewPeriod(String indexCode, String reviewPeriod);

    void deleteByIndexCodeAndReviewPeriod(String indexCode, String reviewPeriod);
}
