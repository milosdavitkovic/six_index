package com.six.indexreview.infrastructure.config;

import lombok.Getter;
import lombok.Setter;

import java.math.RoundingMode;

@Getter
@Setter
public class PrecisionProperties {
    private int internalScale = 16;
    private int outputScale = 10;
    private RoundingMode roundingMode = RoundingMode.HALF_UP;
}
