package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.application.exception.DataImportException;
import com.six.indexreview.domain.model.SecurityId;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Core CompositionCsvReader component for the SIX index review workflow.
 *
 * Kept intentionally concise so the business meaning remains visible
 * without obscuring the implementation.
 */
@Component
public class CompositionCsvReader {
    public List<CompositionRow> read(InputStream inputStream, String fileName) {
        try {
            byte[] content = inputStream.readAllBytes();
            char delimiter = detectDelimiter(content);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8));
                 CSVParser parser = CsvParsingUtils.parser(reader, delimiter)) {
            CsvParsingUtils.requireHeaders(parser, Set.of("id"), fileName);
            List<CompositionRow> rows = new ArrayList<>();
            for (CSVRecord csvRecord : parser) {
                rows.add(new CompositionRow(SecurityId.of(CsvParsingUtils.requiredValue(csvRecord, "id", fileName))));
            }
            if (rows.isEmpty()) {
                throw new DataImportException(fileName + " contains no composition rows");
            }
            return rows;
            }
        } catch (IOException exception) {
            throw new DataImportException("Could not read " + fileName, exception);
        }
    }

    /* Composition supplied by the assignment is a one-column CSV.  Comma and
       semicolon are both accepted to keep the importer useful for future files. */
    private char detectDelimiter(byte[] content) {
        String header = new String(content, 0, Math.min(content.length, 4096), StandardCharsets.UTF_8);
        int semicolon = header.indexOf(';');
        int newline = header.indexOf('\n');
        return semicolon >= 0 && (newline < 0 || semicolon < newline) ? ';' : ',';
    }
}
