package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.SelectedConstituent;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy contract for constituent weight capping.
 *
 * Implementations can introduce alternative capping or redistribution rules
 * without modifying the review pipeline.
 */
public interface WeightCapper {
    /**
     * Applies the configured cap and returns a deterministic allocation.
     */
    List<SelectedConstituent> cap(List<SelectedConstituent> constituents, BigDecimal maxWeight);
}
