package com.six.indexreview.api;

import com.six.indexreview.api.dto.ImportResponse;
import com.six.indexreview.application.CsvImportService;
import com.six.indexreview.application.exception.DataImportException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CsvImportController using Spring Boot WebMvcTest.
 *
 * Validates multipart file upload, response mapping, and error handling.
 */
@WebMvcTest(CsvImportController.class)
@DisplayName("CsvImportController")
class CsvImportControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CsvImportService csvImportService;

    @Test
    @DisplayName("POST /api/import/spi-universe imports file and returns 201 CREATED")
    void importSpiUniverseSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "universe.csv", MediaType.TEXT_PLAIN_VALUE,
                "securityId,date\n1,2026-09-01".getBytes());

        when(csvImportService.importSpiUniverse(any()))
                .thenReturn(new CsvImportService.ImportResult("SPI_UNIVERSE", 2, 1, 0));

        mockMvc.perform(multipart("/api/import/spi-universe")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataset").value("SPI_UNIVERSE"))
                .andExpect(jsonPath("$.inputRows").value(2))
                .andExpect(jsonPath("$.storedRows").value(1));

        verify(csvImportService).importSpiUniverse(any());
    }

    @Test
    @DisplayName("POST /api/import/security-data imports file and returns 201 CREATED")
    void importSecurityDataSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "security.csv", MediaType.TEXT_PLAIN_VALUE,
                "securityId,date,price,freeFloat,shares".getBytes());

        when(csvImportService.importSecurityData(any()))
                .thenReturn(new CsvImportService.ImportResult("SECURITY_DATA", 1, 1, 0));

        mockMvc.perform(multipart("/api/import/security-data")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataset").value("SECURITY_DATA"));

        verify(csvImportService).importSecurityData(any());
    }

    @Test
    @DisplayName("POST /api/import/composition imports file and returns 201 CREATED")
    void importCompositionSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "composition.csv", MediaType.TEXT_PLAIN_VALUE,
                "securityId".getBytes());

        when(csvImportService.importComposition(any()))
                .thenReturn(new CsvImportService.ImportResult("COMPOSITION", 5, 5, 0));

        mockMvc.perform(multipart("/api/import/composition")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataset").value("COMPOSITION"));
    }

    @Test
    @DisplayName("POST /api/import/spi-universe rejects missing file parameter")
    void importSpiUniverseRejectsMissingFile() throws Exception {
        mockMvc.perform(multipart("/api/import/spi-universe"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(csvImportService);
    }

    @Test
    @DisplayName("POST /api/import/spi-universe handles import errors with 400 BAD_REQUEST")
    void importSpiUniverseHandlesDataImportException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "universe.csv", MediaType.TEXT_PLAIN_VALUE,
                "invalid".getBytes());

        when(csvImportService.importSpiUniverse(any()))
                .thenThrow(new DataImportException("Invalid CSV format"));

        mockMvc.perform(multipart("/api/import/spi-universe")
                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/import/all imports all three files and returns combined response")
    void importAllSuccess() throws Exception {
        MockMultipartFile spiUniverse = new MockMultipartFile(
                "spiUniverse", "universe.csv", MediaType.TEXT_PLAIN_VALUE, "data".getBytes());
        MockMultipartFile securityData = new MockMultipartFile(
                "securityData", "security.csv", MediaType.TEXT_PLAIN_VALUE, "data".getBytes());
        MockMultipartFile composition = new MockMultipartFile(
                "composition", "composition.csv", MediaType.TEXT_PLAIN_VALUE, "data".getBytes());

        when(csvImportService.importAll(any(), any(), any()))
                .thenReturn(new CsvImportService.AllImportResult(
                        new CsvImportService.ImportResult("SPI_UNIVERSE", 1, 1, 0),
                        new CsvImportService.ImportResult("SECURITY_DATA", 1, 1, 0),
                        new CsvImportService.ImportResult("COMPOSITION", 1, 1, 0)));

        mockMvc.perform(multipart("/api/import/all")
                .file(spiUniverse)
                .file(securityData)
                .file(composition))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spiUniverse.dataset").value("SPI_UNIVERSE"))
                .andExpect(jsonPath("$.securityData.dataset").value("SECURITY_DATA"))
                .andExpect(jsonPath("$.composition.dataset").value("COMPOSITION"));

        verify(csvImportService).importAll(any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/import/all rejects missing files")
    void importAllRejectsMissingFiles() throws Exception {
        mockMvc.perform(multipart("/api/import/all"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(csvImportService);
    }

    @Test
    @DisplayName("POST /api/import/security-data with conflicting duplicates returns 400")
    void importSecurityDataRejectsDuplicates() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "security.csv", MediaType.TEXT_PLAIN_VALUE, "data".getBytes());

        when(csvImportService.importSecurityData(any()))
                .thenThrow(new DataImportException("Conflicting duplicate market data row"));

        mockMvc.perform(multipart("/api/import/security-data")
                .file(file))
                .andExpect(status().isBadRequest());
    }
}
