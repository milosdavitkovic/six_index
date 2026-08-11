package com.six.indexreview.domain.service;

import com.six.indexreview.domain.model.SelectedConstituent;

import java.math.BigDecimal;
import java.util.List;

public interface WeightCapper {
    List<SelectedConstituent> cap(List<SelectedConstituent> constituents, BigDecimal maxWeight);
}
