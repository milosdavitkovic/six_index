package com.six.indexreview.api;

import com.six.indexreview.api.dto.ImportResponse;
import com.six.indexreview.application.CsvImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvImportControllerTest {
    @Mock
    private CsvImportService csvImportService;

    @Test
    void importEndpointsMapServiceResponsesToResponseObjects() {
        CsvImportController controller = new CsvImportController(csvImportService);
        MockMultipartFile file = file();

        when(csvImportService.importSpiUniverse(file)).thenReturn(new CsvImportService.ImportResult("SPI_UNIVERSE", 3, 2, 1));
        when(csvImportService.importSecurityData(file)).thenReturn(new CsvImportService.ImportResult("SECURITY_DATA", 4, 4, 0));
        when(csvImportService.importComposition(file)).thenReturn(new CsvImportService.ImportResult("COMPOSITION", 5, 3, 2));
        when(csvImportService.importAll(file, file, file)).thenReturn(new CsvImportService.AllImportResult(
                new CsvImportService.ImportResult("SPI_UNIVERSE", 1, 1, 0),
                new CsvImportService.ImportResult("SECURITY_DATA", 1, 1, 0),
                new CsvImportService.ImportResult("COMPOSITION", 1, 1, 0)));

        assertThat(controller.importSpiUniverse(file).dataset()).isEqualTo("SPI_UNIVERSE");
        assertThat(controller.importSecurityData(file).storedRows()).isEqualTo(4);
        assertThat(controller.importComposition(file).deduplicatedRows()).isEqualTo(2);

        assertThat(controller.importAll(file, file, file).spiUniverse().dataset()).isEqualTo("SPI_UNIVERSE");

        verify(csvImportService).importSpiUniverse(file);
        verify(csvImportService).importSecurityData(file);
        verify(csvImportService).importComposition(file);
        verify(csvImportService).importAll(file, file, file);
    }

    @Test
    void allImportResponseRecordExposesItsFields() {
        var response = new CsvImportController.AllImportResponse(new ImportResponse("A", 1, 1, 0), new ImportResponse("B", 2, 1, 1), new ImportResponse("C", 3, 2, 1));

        assertThat(response.spiUniverse().dataset()).isEqualTo("A");
        assertThat(response.securityData().inputRows()).isEqualTo(2);
        assertThat(response.composition().deduplicatedRows()).isEqualTo(1);
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "upload.csv", "text/csv", "x".getBytes(StandardCharsets.UTF_8));
    }
}
