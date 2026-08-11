package com.six.indexreview.infrastructure.persistence.repository;

import com.six.indexreview.infrastructure.persistence.entity.MarketDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MarketDataRepository extends JpaRepository<MarketDataEntity, Long> {
    List<MarketDataEntity> findAllByDate(LocalDate date);
}
