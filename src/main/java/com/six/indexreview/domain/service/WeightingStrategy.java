package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.SelectedConstituent;

import java.util.List;

/**
 * Strategy for calculating raw constituent weights.
 *
 * Alternative weighting methodologies can be introduced through new strategy
 * implementations without changing the review workflow.
 */
public interface WeightingStrategy {
    List<SelectedConstituent> calculate(List<SelectedConstituent> constituents);
}

