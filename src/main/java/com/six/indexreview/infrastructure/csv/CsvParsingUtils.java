package com.six.indexreview.infrastructure.csv;

import com.six.indexreview.application.exception.DataImportException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Milos Davitkovic
 */
public final class CsvParsingUtils {
    private CsvParsingUtils() {
    }

    public static CSVParser parser(Reader reader, char delimiter) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get();
        return format.parse(reader);
    }

    public static Map<String, String> headers(CSVParser parser) {
        Map<String, String> result = new HashMap<>();
        parser.getHeaderMap().keySet().forEach(value -> result.put(normalizeHeader(value), value));
        return result;
    }

    public static void requireHeaders(CSVParser parser, Set<String> required, String fileName) {
        Map<String, String> actual = headers(parser);
        required.stream()
                .filter(requiredColumn -> !actual.containsKey(requiredColumn.toLowerCase(Locale.ROOT)))
                .findFirst()
                .ifPresent(missing -> {
                    throw new DataImportException(fileName + " is missing required column: " + missing);
                });
    }

    public static String value(CSVRecord csvRecord, String column) {
        for (String header : csvRecord.getParser().getHeaderMap().keySet()) {
            if (normalizeHeader(header).equalsIgnoreCase(normalizeHeader(column))) {
                return csvRecord.get(header);
            }
        }
        throw new DataImportException("Missing CSV column: " + column);
    }

    private static String normalizeHeader(String header) {
        return header.replace("\uFEFF", "").trim().toLowerCase(Locale.ROOT);
    }

    public static String requiredValue(CSVRecord csvRecord, String column, String fileName) {
        String value = value(csvRecord, column);
        if (value == null || value.isBlank()) {
            throw new DataImportException(fileName + " contains an empty required value in column " + column
                    + " at row " + csvRecord.getRecordNumber());
        }
        return value.trim();
    }

    public static LocalDate date(CSVRecord csvRecord, String column, String fileName) {
        try {
            return LocalDate.parse(requiredValue(csvRecord, column, fileName));
        } catch (RuntimeException exception) {
            throw new DataImportException(fileName + " contains an invalid date at row " + csvRecord.getRecordNumber(), exception);
        }
    }

    public static BigDecimal decimal(CSVRecord csvRecord, String column, String fileName, boolean required) {
        String value = value(csvRecord, column);
        if (value == null || value.isBlank()) {
            if (required) {
                throw new DataImportException(fileName + " contains an empty required value in column " + column
                        + " at row " + csvRecord.getRecordNumber());
            }
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new DataImportException(fileName + " contains an invalid decimal in column " + column
                    + " at row " + csvRecord.getRecordNumber(), exception);
        }
    }
}
