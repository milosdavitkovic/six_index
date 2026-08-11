package com.six.indexreview.api;

import com.six.indexreview.api.dto.AuditEventResponse;
import com.six.indexreview.api.dto.ReviewResponse;
import com.six.indexreview.application.IndexReviewService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Core IndexReviewController component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@RestController
@RequestMapping("/api/index-reviews")
@RequiredArgsConstructor
@Validated
public class IndexReviewController {
    private final IndexReviewService indexReviewService;

    @PostMapping("/{indexCode}/{reviewPeriod}/run")
    public ReviewResponse run(@PathVariable @NotBlank String indexCode, @PathVariable @NotBlank String reviewPeriod) {
        return indexReviewService.run(indexCode, reviewPeriod);
    }

    @GetMapping("/{indexCode}/{reviewPeriod}/latest")
    public ReviewResponse latest(@PathVariable @NotBlank String indexCode, @PathVariable @NotBlank String reviewPeriod) {
        return indexReviewService.latest(indexCode, reviewPeriod);
    }

    @GetMapping("/results/{reviewResultId}")
    public ReviewResponse byId(@PathVariable @NotNull Long reviewResultId) {
        return indexReviewService.byId(reviewResultId);
    }

    @GetMapping("/results/{reviewResultId}/securities/{securityId}/audit")
    public List<AuditEventResponse> audit(@PathVariable @NotNull Long reviewResultId, @PathVariable int securityId) {
        return indexReviewService.auditForSecurity(reviewResultId, securityId);
    }
}
