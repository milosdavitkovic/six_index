package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.application.exception.DataImportException;
import com.six.indexreview.domain.model.SecurityId;
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

@Component
public class SecurityDataCsvReader {
    public List<SecurityDataRow> read(InputStream inputStream, String fileName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = CsvParsingUtils.parser(reader, ';')) {
            CsvParsingUtils.requireHeaders(parser, Set.of("id", "date", "price", "free_float", "shares"), fileName);
            List<SecurityDataRow> rows = new ArrayList<>();
            for (CSVRecord csvRecord : parser) {
                rows.add(new SecurityDataRow(
                        SecurityId.of(CsvParsingUtils.requiredValue(csvRecord, "id", fileName)),
                        CsvParsingUtils.date(csvRecord, "date", fileName),
                        CsvParsingUtils.decimal(csvRecord, "price", fileName, false),
                        CsvParsingUtils.decimal(csvRecord, "free_float", fileName, false),
                        CsvParsingUtils.decimal(csvRecord, "shares", fileName, false)));
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
