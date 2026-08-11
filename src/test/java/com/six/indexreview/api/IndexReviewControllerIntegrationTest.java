package com.six.indexreview.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.six.indexreview.application.CsvImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IndexReviewControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void importData() throws Exception {
        csvImportService.importSpiUniverse(file("spi_universe.csv"));
        csvImportService.importSecurityData(file("sec_data.csv"));
        csvImportService.importComposition(file("composition.csv"));
    }

    @Test
    void runLatestByIdAndAuditEndpointsReturnBusinessReadableJson() throws Exception {
        MvcResult runResult = mockMvc.perform(post("/api/index-reviews/SMI/Q3-2026/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexCode").value("SMI"))
                .andExpect(jsonPath("$.reviewPeriod").value("Q3-2026"))
                .andExpect(jsonPath("$.status").value("COMPLETED_WITH_WARNINGS"))
                .andExpect(jsonPath("$.constituents.length()").value(20))
                .andExpect(jsonPath("$.joiners", hasItem(177)))
                .andExpect(jsonPath("$.leavers", hasItem(103)))
                .andExpect(jsonPath("$.auditEvents.length()").isNumber())
                .andReturn();

        long reviewResultId = reviewResultId(runResult);

        mockMvc.perform(get("/api/index-reviews/SMI/Q3-2026/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewResultId").value(reviewResultId));

        mockMvc.perform(get("/api/index-reviews/results/{reviewResultId}", reviewResultId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewResultId").value(reviewResultId))
                .andExpect(jsonPath("$.constituents.length()").value(20));

        mockMvc.perform(get("/api/index-reviews/results/{reviewResultId}/securities/{securityId}/audit", reviewResultId, 177))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].securityId").value(177))
                .andExpect(jsonPath("$[0].ruleCode").isNotEmpty())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));

        mockMvc.perform(get("/api/index-reviews/results/{reviewResultId}/securities/{securityId}/audit", reviewResultId, 999999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void invalidConfigurationsAndMissingResourcesAreRenderedAsApiErrors() throws Exception {
        mockMvc.perform(post("/api/index-reviews/SMIM/Q3-2026/run"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));


        mockMvc.perform(get("/api/index-reviews/SMI/Q3-2026/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));

        mockMvc.perform(get("/api/index-reviews/results/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));

        mockMvc.perform(get("/api/index-reviews/results/999999/securities/177/audit"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    @Test
    void runReturnsValidationErrorWhenCurrentCompositionIsEmpty() throws Exception {
        jdbcTemplate.update("DELETE FROM index_composition WHERE index_code = ? AND review_period = ?", "SMI", "Q3-2026");

        mockMvc.perform(post("/api/index-reviews/SMI/Q3-2026/run"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errorCode").value("REVIEW_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors[0].code").exists())
                .andExpect(jsonPath("$.validationErrors[0].severity").value("ERROR"));
    }

    private MockMultipartFile file(String name) throws Exception {
        return new MockMultipartFile("file", name, "text/csv", Files.readAllBytes(Path.of("data", name)));
    }

    private long reviewResultId(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("reviewResultId").asLong();
    }
}
