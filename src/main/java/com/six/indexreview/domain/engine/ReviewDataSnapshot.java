package com.six.indexreview.domain.engine;

import com.six.indexreview.domain.model.MarketData;
import com.six.indexreview.domain.model.SecurityId;

import java.util.Map;
import java.util.Set;

public record ReviewDataSnapshot(
        Set<SecurityId> universe,
        Map<SecurityId, MarketData> cutOffMarketData,
        Map<SecurityId, MarketData> reviewMarketData,
        Set<SecurityId> currentComposition) {
}
