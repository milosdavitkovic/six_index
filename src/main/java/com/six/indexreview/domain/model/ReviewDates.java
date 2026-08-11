package com.six.indexreview.domain.model;

import java.time.LocalDate;

public record ReviewDates(LocalDate cutOffDate, LocalDate reviewDate) {
    public ReviewDates {
        if (cutOffDate == null || reviewDate == null) {
            throw new IllegalArgumentException("Both review dates are required");
        }
        if (!cutOffDate.isBefore(reviewDate)) {
            throw new IllegalArgumentException("Cut-off date must be before review date");
        }
    }
}
