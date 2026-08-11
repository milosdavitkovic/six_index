package com.six.indexreview.application;

import com.six.indexreview.application.exception.DataImportException;
import com.six.indexreview.domain.model.IndexCode;
import com.six.indexreview.domain.model.IndexDefinition;
import com.six.indexreview.domain.model.ReviewDates;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.infrastructure.config.IndexDefinitionProvider;
import com.six.indexreview.infrastructure.csv.CompositionCsvReader;
import com.six.indexreview.infrastructure.csv.CompositionRow;
import com.six.indexreview.infrastructure.csv.SecurityDataCsvReader;
import com.six.indexreview.infrastructure.csv.SecurityDataRow;
import com.six.indexreview.infrastructure.csv.SpiUniverseCsvReader;
import com.six.indexreview.infrastructure.csv.SpiUniverseRow;
import com.six.indexreview.infrastructure.persistence.entity.IndexCompositionEntity;
import com.six.indexreview.infrastructure.persistence.entity.MarketDataEntity;
import com.six.indexreview.infrastructure.persistence.entity.SecurityEntity;
import com.six.indexreview.infrastructure.persistence.entity.SpiUniverseMemberEntity;
import com.six.indexreview.infrastructure.persistence.repository.IndexCompositionRepository;
import com.six.indexreview.infrastructure.persistence.repository.MarketDataRepository;
import com.six.indexreview.infrastructure.persistence.repository.SecurityRepository;
import com.six.indexreview.infrastructure.persistence.repository.SpiUniverseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@SpringBootTest(classes = CsvImportService.class)
class CsvImportServiceTest {
    @Autowired
    private CsvImportService csvImportService;

    @MockBean
    private SpiUniverseCsvReader spiUniverseCsvReader;

    @MockBean
    private SecurityDataCsvReader securityDataCsvReader;

    @MockBean
    private CompositionCsvReader compositionCsvReader;

    @MockBean
    private SecurityRepository securityRepository;

    @MockBean
    private SpiUniverseRepository spiUniverseRepository;

    @MockBean
    private MarketDataRepository marketDataRepository;

    @MockBean
    private IndexCompositionRepository indexCompositionRepository;

    @MockBean
    private IndexDefinitionProvider indexDefinitionProvider;

    @Test
    void importSpiUniverseDeduplicatesIdenticalRowsAndPersistsMissingSecurities() throws Exception {
        var date = LocalDate.of(2026, 9, 1);
        when(spiUniverseCsvReader.read(any(), any())).thenReturn(List.of(
                new SpiUniverseRow(date, new SecurityId(1)),
                new SpiUniverseRow(date, new SecurityId(1)),
                new SpiUniverseRow(date.plusDays(1), new SecurityId(2))));
        when(securityRepository.findAllById(anyList())).thenReturn(List.of(new SecurityEntity(1)));

        var result = csvImportService.importSpiUniverse(file("spi-universe.csv", "x"));

        assertThat(result.dataset()).isEqualTo("SPI_UNIVERSE");
        assertThat(result.inputRows()).isEqualTo(3);
        assertThat(result.storedRows()).isEqualTo(2);
        assertThat(result.deduplicatedRows()).isEqualTo(1);
        verify(spiUniverseRepository).deleteAllInBatch();
        ArgumentCaptor<List<SpiUniverseMemberEntity>> spiCaptor = ArgumentCaptor.forClass(List.class);
        verify(spiUniverseRepository).saveAll(spiCaptor.capture());
        assertThat(spiCaptor.getValue()).hasSize(2);
        assertThat(spiCaptor.getValue().stream().map(SpiUniverseMemberEntity::getSecurityId).toList()).containsExactly(1, 2);

        ArgumentCaptor<List<SecurityEntity>> securityCaptor = ArgumentCaptor.forClass(List.class);
        verify(securityRepository).saveAll(securityCaptor.capture());
        assertThat(securityCaptor.getValue()).hasSize(1);
        assertThat(securityCaptor.getValue().get(0).getId()).isEqualTo(2);
    }

    @Test
    void importSecurityDataRejectsConflictingDuplicateRows() throws Exception {
        var date = LocalDate.of(2026, 9, 1);
        when(securityDataCsvReader.read(any(), any())).thenReturn(List.of(
                new SecurityDataRow(new SecurityId(1), date, new BigDecimal("10"), new BigDecimal("0.5"), new BigDecimal("100")),
                new SecurityDataRow(new SecurityId(1), date, new BigDecimal("11"), new BigDecimal("0.5"), new BigDecimal("100"))));

        assertThatThrownBy(() -> csvImportService.importSecurityData(file("security-data.csv", "x")))
                .isInstanceOf(DataImportException.class)
                .hasMessageContaining("Conflicting duplicate market data row");
        verifyNoInteractions(marketDataRepository);
        verifyNoInteractions(securityRepository);
    }

    @Test
    void importCompositionUsesConfiguredDefinitionAndSelfDelegatingVariantWorks() throws Exception {
        var definition = new IndexDefinition(new IndexCode("SMI"), "Swiss Market Index", 20, new BigDecimal("0.18"),
                "FFMCAP", "TOP_N", "NONE", "Q3-2026",
                new ReviewDates(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 21)), List.of("SECURITY_ID_ASC"), true, 0);
        when(indexDefinitionProvider.get("SMI", "Q3-2026")).thenReturn(definition);
        when(compositionCsvReader.read(any(), any())).thenReturn(List.of(
                new CompositionRow(new SecurityId(10)),
                new CompositionRow(new SecurityId(10)),
                new CompositionRow(new SecurityId(11))));
        when(securityRepository.findAllById(anyList())).thenReturn(List.of());

        var result = csvImportService.importComposition(file("composition.csv", "x"));

        assertThat(result.dataset()).isEqualTo("COMPOSITION");
        assertThat(result.inputRows()).isEqualTo(3);
        assertThat(result.storedRows()).isEqualTo(2);
        assertThat(result.deduplicatedRows()).isEqualTo(1);
        verify(indexCompositionRepository).deleteByIndexCodeAndReviewPeriod("SMI", "Q3-2026");
        ArgumentCaptor<List<IndexCompositionEntity>> compositionCaptor = ArgumentCaptor.forClass(List.class);
        verify(indexCompositionRepository).saveAll(compositionCaptor.capture());
        assertThat(compositionCaptor.getValue()).hasSize(2);
        assertThat(compositionCaptor.getValue().stream().map(IndexCompositionEntity::getSecurityId).toList()).containsExactly(10, 11);

        ArgumentCaptor<List<SecurityEntity>> compositionSecurityCaptor = ArgumentCaptor.forClass(List.class);
        verify(securityRepository).saveAll(compositionSecurityCaptor.capture());
        assertThat(compositionSecurityCaptor.getValue()).hasSize(2);
        assertThat(compositionSecurityCaptor.getValue().stream().map(SecurityEntity::getId).toList()).containsExactly(10, 11);
    }

    @Test
    void importAllCombinesTheThreeImportResults() throws Exception {
        when(spiUniverseCsvReader.read(any(), any())).thenReturn(List.of(new SpiUniverseRow(LocalDate.of(2026, 9, 1), new SecurityId(1))));
        when(securityDataCsvReader.read(any(), any())).thenReturn(List.of(new SecurityDataRow(new SecurityId(1), LocalDate.of(2026, 9, 1), new BigDecimal("10"), new BigDecimal("0.5"), new BigDecimal("100"))));
        when(indexDefinitionProvider.get("SMI", "Q3-2026")).thenReturn(new IndexDefinition(new IndexCode("SMI"), "Swiss Market Index", 20,
                new BigDecimal("0.18"), "FFMCAP", "TOP_N", "NONE", "Q3-2026",
                new ReviewDates(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 21)), List.of(), true, 0));
        when(compositionCsvReader.read(any(), any())).thenReturn(List.of(new CompositionRow(new SecurityId(1))));
        when(securityRepository.findAllById(anyList())).thenReturn(List.of());

        var result = csvImportService.importAll(file("spi-universe.csv", "x"), file("security-data.csv", "x"), file("composition.csv", "x"));

        assertThat(result.spiUniverse().storedRows()).isEqualTo(1);
        assertThat(result.securityData().storedRows()).isEqualTo(1);
        assertThat(result.composition().storedRows()).isEqualTo(1);
    }

    @Test
    void emptyUploadsAreRejected() {
        assertThatThrownBy(() -> csvImportService.importSpiUniverse(new MockMultipartFile("file", new byte[0])))
                .isInstanceOf(DataImportException.class)
                .hasMessageContaining("must not be empty");
    }

    private MockMultipartFile file(String name, String content) {
        return new MockMultipartFile("file", name, "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }
}
