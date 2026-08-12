package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.application.exception.DataImportException;
import com.six.indexreview.domain.model.SecurityId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Core SpiUniverseCsvReader component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
/**
 * @author Milos Davitkovic
 */
@Slf4j
@Component
public class SpiUniverseCsvReader {
    public List<SpiUniverseRow> read(InputStream inputStream, String fileName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = CsvParsingUtils.parser(reader, ';')) {
            CsvParsingUtils.requireHeaders(parser, Set.of("date", "id"), fileName);
            List<SpiUniverseRow> rows = new ArrayList<>();
            for (CSVRecord csvRecord : parser) {
                rows.add(new SpiUniverseRow(CsvParsingUtils.date(csvRecord, "date", fileName),
                        SecurityId.of(CsvParsingUtils.requiredValue(csvRecord, "id", fileName))));
            }
            if (rows.isEmpty()) {
                throw new DataImportException(fileName + " contains no data rows");
            }
            return rows;
        } catch (IOException exception) {
            throw new DataImportException("Could not read " + fileName, exception);
        }
    }
}
