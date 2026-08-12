package com.six.indexreview.api;

import com.six.indexreview.api.dto.ImportResponse;
import com.six.indexreview.application.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Core CsvImportController component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class CsvImportController {
    private final CsvImportService csvImportService;

    @PostMapping(value = "/spi-universe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImportResponse importSpiUniverse(@RequestParam("file") MultipartFile file) {
        return toResponse(csvImportService.importSpiUniverse(file));
    }

    @PostMapping(value = "/security-data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImportResponse importSecurityData(@RequestParam("file") MultipartFile file) {
        return toResponse(csvImportService.importSecurityData(file));
    }

    @PostMapping(value = "/composition", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImportResponse importComposition(@RequestParam("file") MultipartFile file) {
        return toResponse(csvImportService.importComposition(file));
    }

    @PostMapping(value = "/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AllImportResponse importAll(@RequestParam("spiUniverse") MultipartFile spiUniverse,
                                       @RequestParam("securityData") MultipartFile securityData,
                                       @RequestParam("composition") MultipartFile composition) {
        var result = csvImportService.importAll(spiUniverse, securityData, composition);
        return new AllImportResponse(toResponse(result.spiUniverse()), toResponse(result.securityData()),
                toResponse(result.composition()));
    }

    private ImportResponse toResponse(CsvImportService.ImportResult result) {
        return new ImportResponse(result.dataset(), result.inputRows(), result.storedRows(), result.deduplicatedRows());
    }

    public record AllImportResponse(ImportResponse spiUniverse, ImportResponse securityData,
                                    ImportResponse composition) {
    }
}
