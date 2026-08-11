package com.six.indexreview.infrastructure.csv;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvReaderTest {
    @Test
    void readsSemicolonDataWithUtf8BomAndEmptyPrice() {
        String csv = "\uFEFFid;date;price;free_float;shares\n1;2026-09-21;;0.5;100\n";
        var rows = new SecurityDataCsvReader().read(stream(csv), "security.csv");
        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.price()).isNull();
            assertThat(row.freeFloat()).isEqualByComparingTo("0.5");
        });
    }

    @Test
    void rejectsMissingRequiredColumn() {
        assertThatThrownBy(() -> new SpiUniverseCsvReader().read(stream("id\n1\n"), "spi.csv"))
                .isInstanceOf(com.six.indexreview.application.exception.DataImportException.class)
                .hasMessageContaining("missing required column");
    }

    @Test
    void readsAndDeduplicationCanBeAppliedByImportService() {
        var rows = new SpiUniverseCsvReader().read(stream("date;id\n2026-09-21;1\n2026-09-21;1\n"), "spi.csv");
        assertThat(rows).hasSize(2).allMatch(value -> value.securityId().value() == 1);
    }

    private ByteArrayInputStream stream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
