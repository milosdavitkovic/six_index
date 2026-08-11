package com.six.indexreview.infrastructure.config;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Core IndexConfiguration component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Getter
@Setter
public class IndexConfiguration {
    private String indexCode;
    private String name;
    private int constituentCount;
    private BigDecimal maxWeight;
    private String rankingRule = "FFMCAP";
    private String selectionRule = "TOP_N";
    private String bufferRule = "NONE";
    private String reviewPeriod;
    private LocalDate cutOffDate;
    private LocalDate reviewDate;
    private boolean enabled = true;
    private List<String> tieBreakers = new ArrayList<>();
    private int bufferRetentionRank;
}
