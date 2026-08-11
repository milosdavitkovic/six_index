package com.six.indexreview.api;

import com.six.indexreview.api.dto.AuditEventResponse;
import com.six.indexreview.api.dto.ReviewResponse;
import com.six.indexreview.application.IndexReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/index-reviews")
@RequiredArgsConstructor
public class IndexReviewController {
    private final IndexReviewService indexReviewService;

    @PostMapping("/{indexCode}/{reviewPeriod}/run")
    public ReviewResponse run(@PathVariable String indexCode, @PathVariable String reviewPeriod) {
        return indexReviewService.run(indexCode, reviewPeriod);
    }

    @GetMapping("/{indexCode}/{reviewPeriod}/latest")
    public ReviewResponse latest(@PathVariable String indexCode, @PathVariable String reviewPeriod) {
        return indexReviewService.latest(indexCode, reviewPeriod);
    }

    @GetMapping("/results/{reviewResultId}")
    public ReviewResponse byId(@PathVariable Long reviewResultId) {
        return indexReviewService.byId(reviewResultId);
    }

    @GetMapping("/results/{reviewResultId}/securities/{securityId}/audit")
    public List<AuditEventResponse> audit(@PathVariable Long reviewResultId, @PathVariable int securityId) {
        return indexReviewService.auditForSecurity(reviewResultId, securityId);
    }
}
