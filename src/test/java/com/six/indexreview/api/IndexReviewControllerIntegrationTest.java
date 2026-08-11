package com.six.indexreview.api;

import com.six.indexreview.application.CsvImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IndexReviewControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CsvImportService csvImportService;

    @BeforeEach
    void importData() throws Exception {
        csvImportService.importSpiUniverse(file("spi_universe.csv"));
        csvImportService.importSecurityData(file("sec_data.csv"));
        csvImportService.importComposition(file("composition.csv"));
    }

    @Test
    void healthAndRunEndpointsReturnBusinessReadableJson() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(post("/api/index-reviews/SMI/Q3-2026/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexCode").value("SMI"))
                .andExpect(jsonPath("$.constituents.length()").value(20))
                .andExpect(jsonPath("$.joiners", hasItem(177)))
                .andExpect(jsonPath("$.leavers", hasItem(103)))
                .andExpect(jsonPath("$.auditEvents.length()").isNumber());
    }

    @Test
    void multipartImportAllAndUnknownResultAreHandled() throws Exception {
        mockMvc.perform(multipart("/api/import/all")
                        .file(file("spi_universe.csv", "spiUniverse"))
                        .file(file("sec_data.csv", "securityData"))
                        .file(file("composition.csv", "composition")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spiUniverse.storedRows").value(205));

        mockMvc.perform(get("/api/index-reviews/results/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
    }

    private MockMultipartFile file(String name) throws Exception {
        return file(name, "file");
    }

    private MockMultipartFile file(String name, String field) throws Exception {
        return new MockMultipartFile(field, name, "text/csv", Files.readAllBytes(Path.of("data", name)));
    }
}
