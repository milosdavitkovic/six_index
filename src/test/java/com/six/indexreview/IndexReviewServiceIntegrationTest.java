package com.six.indexreview;

import com.six.indexreview.api.dto.ReviewResponse;
import com.six.indexreview.application.CsvImportService;
import com.six.indexreview.application.IndexReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IndexReviewServiceIntegrationTest {
    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private IndexReviewService indexReviewService;

    @Test
    void suppliedQ3DataProducesExpectedPureTopTwentyReview() throws Exception {
        csvImportService.importSpiUniverse(file("spi_universe.csv"));
        csvImportService.importSecurityData(file("sec_data.csv"));
        csvImportService.importComposition(file("composition.csv"));

        ReviewResponse response = indexReviewService.run("SMI", "Q3-2026");

        assertThat(response.status()).isEqualTo("COMPLETED_WITH_WARNINGS");
        assertThat(response.totalEligibleSecurities()).isEqualTo(204);
        assertThat(response.totalSelectedConstituents()).isEqualTo(20);
        assertThat(response.constituents().stream().map(value -> value.securityId()).toList())
                .containsExactly(155, 205, 63, 64, 65, 54, 177, 176, 70, 87, 280, 309, 317, 270, 95, 43, 311, 253, 249, 28);
        assertThat(response.joiners()).containsExactly(28, 177, 249);
        assertThat(response.leavers()).containsExactly(81, 103, 160);
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
}
