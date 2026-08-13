package com.six.indexreview.domain.engine;

import com.six.indexreview.domain.model.MarketData;
import com.six.indexreview.domain.model.SecurityId;

import java.util.Map;
import java.util.Set;

/**
 * Immutable data carrier for ReviewDataSnapshot.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 * @author Milos Davitkovic
 */
public record ReviewDataSnapshot(
        Set<SecurityId> universe,
        Map<SecurityId, MarketData> cutOffMarketData,
        Map<SecurityId, MarketData> reviewMarketData,
        Set<SecurityId> currentComposition) {
}
