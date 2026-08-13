package com.six.indexreview;

import com.six.indexreview.api.dto.ReviewResponse;
import com.six.indexreview.application.CsvImportService;
import com.six.indexreview.application.IndexReviewService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Milos Davitkovic
 */
@SpringBootTest
class IndexReviewServiceIntegrationTest {
    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private IndexReviewService indexReviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void q3_2026LiveRunProducesExpectedConstituentsAndWeights() throws Exception {
        csvImportService.importSpiUniverse(file("spi_universe.csv"));
        csvImportService.importSecurityData(file("sec_data.csv"));
        csvImportService.importComposition(file("composition.csv"));

        ReviewResponse response = indexReviewService.run("SMI", "Q3-2026");
        ExpectedConstituent[] expected = expectedConstituents();

        assertThat(response.status()).isEqualTo("COMPLETED_WITH_WARNINGS");
        assertThat(response.totalEligibleSecurities()).isEqualTo(204);
        assertThat(response.totalSelectedConstituents()).isEqualTo(20);
        assertThat(response.constituents()).hasSize(expected.length);
        for (int index = 0; index < expected.length; index++) {
            assertThat(response.constituents().get(index).securityId()).isEqualTo(expected[index].securityId());
            assertThat(response.constituents().get(index).finalWeight())
                    .isEqualByComparingTo(expected[index].finalWeight());
        }
        assertThat(response.joiners()).containsExactly(28, 177, 249);
        assertThat(response.leavers()).containsExactly(81, 103, 160);
        assertThat(response.constituents()).allSatisfy(value -> {
            assertThat(value.ffmcap()).isPositive();
            assertThat(value.finalWeight().scale()).isEqualTo(10);
        });
        assertThat(response.constituents().stream().map(Constituent -> Constituent.finalWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("1.0000000000");
        assertThat(response.constituents()).allSatisfy(value ->
                assertThat(value.finalWeight()).isLessThanOrEqualTo(new BigDecimal("0.1800000000")));

        ReviewResponse latest = indexReviewService.latest("SMI", "Q3-2026");
        assertThat(latest.reviewResultId()).isEqualTo(response.reviewResultId());

        ReviewResponse latestLowerCase = indexReviewService.latest("SMI", "q3-2026");
        assertThat(latestLowerCase.reviewResultId()).isEqualTo(response.reviewResultId());

        assertThat(indexReviewService.auditForSecurity(response.reviewResultId(), 177)).isNotEmpty();
    }

    private MockMultipartFile file(String name) throws Exception {
        Path path = Path.of("data", name);
        return new MockMultipartFile("file", name, "text/csv", Files.readAllBytes(path));
    }

    private ExpectedConstituent[] expectedConstituents() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/expected-smi-q3-2026.json")) {
            assertThat(input).as("expected SMI Q3-2026 fixture").isNotNull();
            return objectMapper.readValue(input, ExpectedConstituent[].class);
        }
    }

    private record ExpectedConstituent(int securityId, BigDecimal finalWeight) {
    }
}
