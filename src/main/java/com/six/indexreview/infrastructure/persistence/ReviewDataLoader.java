package com.six.indexreview.infrastructure.persistence;

import com.six.indexreview.domain.engine.ReviewDataSnapshot;
import com.six.indexreview.domain.model.MarketData;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.infrastructure.persistence.entity.IndexCompositionEntity;
import com.six.indexreview.infrastructure.persistence.entity.MarketDataEntity;
import com.six.indexreview.infrastructure.persistence.entity.SpiUniverseMemberEntity;
import com.six.indexreview.infrastructure.persistence.repository.IndexCompositionRepository;
import com.six.indexreview.infrastructure.persistence.repository.MarketDataRepository;
import com.six.indexreview.infrastructure.persistence.repository.SpiUniverseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Core ReviewDataLoader component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Component
@RequiredArgsConstructor
public class ReviewDataLoader {
    private final SpiUniverseRepository spiUniverseRepository;
    private final MarketDataRepository marketDataRepository;
    private final IndexCompositionRepository indexCompositionRepository;

    public ReviewDataSnapshot load(String indexCode, String reviewPeriod, LocalDate cutOffDate, LocalDate reviewDate) {
        Set<SecurityId> universe = new LinkedHashSet<>();
        java.util.List<SpiUniverseMemberEntity> universeEntities = spiUniverseRepository.findAllByDate(reviewDate);
        if (universeEntities == null) {
            throw new IllegalStateException("spiUniverseRepository returned null");
        }
        for (SpiUniverseMemberEntity entity : universeEntities) {
            universe.add(new SecurityId(entity.getSecurityId()));
        }
        Map<SecurityId, MarketData> cutOff = toMap(marketDataRepository.findAllByDate(cutOffDate));
        Map<SecurityId, MarketData> review = toMap(marketDataRepository.findAllByDate(reviewDate));
        Set<SecurityId> composition = new LinkedHashSet<>();
        java.util.List<IndexCompositionEntity> compositionEntities =
                indexCompositionRepository.findAllByIndexCodeAndReviewPeriod(indexCode, reviewPeriod);
        if (compositionEntities == null) {
            throw new IllegalStateException("indexCompositionRepository returned null");
        }
        compositionEntities.forEach(entity -> composition.add(new SecurityId(entity.getSecurityId())));
        return new ReviewDataSnapshot(universe, cutOff, review, composition);
    }

    private Map<SecurityId, MarketData> toMap(java.util.List<MarketDataEntity> entities) {
        if (entities == null) {
            throw new IllegalStateException("marketDataRepository returned null");
        }
        Map<SecurityId, MarketData> result = new LinkedHashMap<>();
        for (MarketDataEntity entity : entities) {
            SecurityId id = new SecurityId(entity.getSecurityId());
            result.put(id, new MarketData(id, entity.getDate(), entity.getPrice(), entity.getShares(), entity.getFreeFloat()));
        }
        return result;
    }
}
