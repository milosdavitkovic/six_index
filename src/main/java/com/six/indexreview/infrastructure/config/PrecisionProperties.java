package com.six.indexreview.infrastructure.config;

import lombok.Getter;
import lombok.Setter;

import java.math.RoundingMode;

/**
 * Core PrecisionProperties component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Getter
@Setter
public class PrecisionProperties {
    private int internalScale = 16;
    private int outputScale = 10;
    private RoundingMode roundingMode = RoundingMode.HALF_UP;
}
