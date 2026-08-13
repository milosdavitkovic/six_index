package com.six.indexreview.api;

import com.six.indexreview.api.dto.ReviewResponse;
import com.six.indexreview.application.IndexReviewService;
import com.six.indexreview.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for IndexReviewController using Spring Boot WebMvcTest.
 *
 * Validates HTTP endpoints, parameter validation, exception handling, and response formats.
 */
@WebMvcTest(IndexReviewController.class)
@Import(MvcTestApplicationConfiguration.class)
@DisplayName("IndexReviewController")
class IndexReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IndexReviewService indexReviewService;

    private static final ReviewResponse SAMPLE_REVIEW = new ReviewResponse(
            1L, "SMI", "Q3-2026",
            LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 21),
            "COMPLETED", Instant.now(), 100, 20,
            List.of(), List.of(), List.of(), List.of(),
            List.of(), List.of(), List.of(), List.of(), List.of()
    );

    @Test
    @DisplayName("POST /api/index-reviews/{indexCode}/{reviewPeriod}/run returns 200 and ReviewResponse")
    void runReviewEndpointSuccess() throws Exception {
        when(indexReviewService.run("SMI", "Q3-2026"))
                .thenReturn(SAMPLE_REVIEW);

        mockMvc.perform(post("/api/index-reviews/SMI/Q3-2026/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewResultId").value(1))
                .andExpect(jsonPath("$.indexCode").value("SMI"))
                .andExpect(jsonPath("$.reviewPeriod").value("Q3-2026"));

        verify(indexReviewService).run("SMI", "Q3-2026");
    }

    @Test
    @DisplayName("POST /api/index-reviews/{indexCode}/{reviewPeriod}/run rejects blank indexCode")
    void runReviewRejectBlankIndexCode() throws Exception {
        mockMvc.perform(post("/api/index-reviews/ /Q3-2026/run"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(indexReviewService);
    }

    @Test
    @DisplayName("POST /api/index-reviews/{indexCode}/{reviewPeriod}/run rejects blank reviewPeriod")
    void runReviewRejectBlankReviewPeriod() throws Exception {
        mockMvc.perform(post("/api/index-reviews/SMI/ /run"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(indexReviewService);
    }

    @Test
    @DisplayName("GET /api/index-reviews/{indexCode}/{reviewPeriod}/latest returns latest review")
    void latestReviewEndpointSuccess() throws Exception {
        when(indexReviewService.latest("SMI", "Q3-2026"))
                .thenReturn(SAMPLE_REVIEW);

        mockMvc.perform(get("/api/index-reviews/SMI/Q3-2026/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexCode").value("SMI"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(indexReviewService).latest("SMI", "Q3-2026");
    }

    @Test
    @DisplayName("GET /api/index-reviews/{indexCode}/{reviewPeriod}/latest throws 404 when not found")
    void latestReviewNotFound() throws Exception {
        when(indexReviewService.latest(anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("SMI/Q3-2026 not found"));

        mockMvc.perform(get("/api/index-reviews/SMI/Q3-2026/latest"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/index-reviews/results/{reviewResultId} returns review by ID")
    void byIdReviewEndpointSuccess() throws Exception {
        when(indexReviewService.byId(1L))
                .thenReturn(SAMPLE_REVIEW);

        mockMvc.perform(get("/api/index-reviews/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewResultId").value(1));

        verify(indexReviewService).byId(1L);
    }

    @Test
    @DisplayName("GET /api/index-reviews/results/{reviewResultId} rejects null ID")
    void byIdReviewRejectNullId() throws Exception {
        mockMvc.perform(get("/api/index-reviews/results/null"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(indexReviewService);
    }

    @Test
    @DisplayName("GET /api/index-reviews/results/{reviewResultId} throws 404 when not found")
    void byIdReviewNotFound() throws Exception {
        when(indexReviewService.byId(999L))
                .thenThrow(new ResourceNotFoundException("Review 999 not found"));

        mockMvc.perform(get("/api/index-reviews/results/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/index-reviews/results/{reviewResultId}/securities/{securityId}/audit returns audit events")
    void auditForSecurityEndpointSuccess() throws Exception {
        when(indexReviewService.auditForSecurity(1L, 42))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/index-reviews/results/1/securities/42/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(indexReviewService).auditForSecurity(1L, 42);
    }

    @Test
    @DisplayName("GET /api/index-reviews/results/{reviewResultId}/securities/{securityId}/audit throws 404 when not found")
    void auditForSecurityNotFound() throws Exception {
        when(indexReviewService.auditForSecurity(999L, 42))
                .thenThrow(new ResourceNotFoundException("Audit trail not found"));

        mockMvc.perform(get("/api/index-reviews/results/999/securities/42/audit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/index-reviews/{indexCode}/{reviewPeriod}/run propagates service exceptions")
    void runReviewPropagatesServiceException() throws Exception {
        when(indexReviewService.run("INVALID", "Q3-2026"))
                .thenThrow(new IllegalArgumentException("Unknown index code"));

        mockMvc.perform(post("/api/index-reviews/INVALID/Q3-2026/run"))
                .andExpect(status().isBadRequest());
    }
}
