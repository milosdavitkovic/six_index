package com.six.indexreview.application;

import com.six.indexreview.application.exception.DataImportException;
import com.six.indexreview.domain.model.SecurityId;
import com.six.indexreview.infrastructure.config.IndexDefinitionProvider;
import com.six.indexreview.infrastructure.csv.*;
import com.six.indexreview.infrastructure.persistence.entity.IndexCompositionEntity;
import com.six.indexreview.infrastructure.persistence.entity.MarketDataEntity;
import com.six.indexreview.infrastructure.persistence.entity.SecurityEntity;
import com.six.indexreview.infrastructure.persistence.entity.SpiUniverseMemberEntity;
import com.six.indexreview.infrastructure.persistence.repository.IndexCompositionRepository;
import com.six.indexreview.infrastructure.persistence.repository.MarketDataRepository;
import com.six.indexreview.infrastructure.persistence.repository.SecurityRepository;
import com.six.indexreview.infrastructure.persistence.repository.SpiUniverseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class CsvImportService {
    private final SpiUniverseCsvReader spiUniverseCsvReader;
    private final SecurityDataCsvReader securityDataCsvReader;
    private final CompositionCsvReader compositionCsvReader;
    private final SecurityRepository securityRepository;
    private final SpiUniverseRepository spiUniverseRepository;
    private final MarketDataRepository marketDataRepository;
    private final IndexCompositionRepository indexCompositionRepository;
    private final IndexDefinitionProvider indexDefinitionProvider;
    @Lazy
    private final CsvImportService self;

    public CsvImportService(SpiUniverseCsvReader spiUniverseCsvReader,
                            SecurityDataCsvReader securityDataCsvReader,
                            CompositionCsvReader compositionCsvReader,
                            SecurityRepository securityRepository,
                            SpiUniverseRepository spiUniverseRepository,
                            MarketDataRepository marketDataRepository,
                            IndexCompositionRepository indexCompositionRepository,
                            IndexDefinitionProvider indexDefinitionProvider,
                            @Lazy CsvImportService self) {
        this.spiUniverseCsvReader = spiUniverseCsvReader;
        this.securityDataCsvReader = securityDataCsvReader;
        this.compositionCsvReader = compositionCsvReader;
        this.securityRepository = securityRepository;
        this.spiUniverseRepository = spiUniverseRepository;
        this.marketDataRepository = marketDataRepository;
        this.indexCompositionRepository = indexCompositionRepository;
        this.indexDefinitionProvider = indexDefinitionProvider;
        this.self = self;
    }

    @Transactional
    public ImportResult importSpiUniverse(MultipartFile file) {
        List<SpiUniverseRow> rows = read(file, spiUniverseCsvReader::read);
        Map<String, SpiUniverseRow> unique = new LinkedHashMap<>();
        int duplicates = 0;
        for (SpiUniverseRow row : rows) {
            String key = row.securityId() + "|" + row.date();
            if (unique.containsKey(key)) {
                if (!unique.get(key).equals(row)) {
                    throw new DataImportException("Conflicting duplicate SPI universe row for " + key);
                }
                duplicates++;
            } else {
                unique.put(key, row);
            }
        }
        if (duplicates > 0) {
            log.warn("Deduplicated {} identical SPI universe rows", duplicates);
        }
        spiUniverseRepository.deleteAllInBatch();
        List<SpiUniverseMemberEntity> entities = unique.values().stream()
                .map(row -> new SpiUniverseMemberEntity(row.securityId().value(), row.date()))
                .toList();
        spiUniverseRepository.saveAll(entities);
        saveSecurities(unique.values().stream().map(SpiUniverseRow::securityId).toList());
        log.info("Imported SPI universe rows={} uniqueRows={}", rows.size(), entities.size());
        return new ImportResult("SPI_UNIVERSE", rows.size(), entities.size(), duplicates);
    }

    @Transactional
    public ImportResult importSecurityData(MultipartFile file) {
        List<SecurityDataRow> rows = read(file, securityDataCsvReader::read);
        Map<String, SecurityDataRow> unique = new LinkedHashMap<>();
        int duplicates = 0;
        for (SecurityDataRow row : rows) {
            String key = row.securityId() + "|" + row.date();
            if (unique.containsKey(key)) {
                if (!sameMarketData(unique.get(key), row)) {
                    throw new DataImportException("Conflicting duplicate market data row for " + key);
                }
                duplicates++;
            } else {
                unique.put(key, row);
            }
        }
        if (duplicates > 0) {
            log.warn("Deduplicated {} identical market data rows", duplicates);
        }
        marketDataRepository.deleteAllInBatch();
        List<MarketDataEntity> entities = unique.values().stream()
                .map(row -> new MarketDataEntity(row.securityId().value(), row.date(), row.price(),
                        row.shares(), row.freeFloat()))
                .toList();
        marketDataRepository.saveAll(entities);
        saveSecurities(unique.values().stream().map(SecurityDataRow::securityId).toList());
        log.info("Imported market data rows={} uniqueRows={}", rows.size(), entities.size());
        return new ImportResult("SECURITY_DATA", rows.size(), entities.size(), duplicates);
    }

    @Transactional
    public ImportResult importComposition(MultipartFile file, String indexCode, String reviewPeriod) {
        List<CompositionRow> rows = read(file, compositionCsvReader::read);
        Set<SecurityId> unique = new LinkedHashSet<>();
        for (CompositionRow row : rows) {
            if (!unique.add(row.securityId())) {
                log.warn("Deduplicated duplicate composition securityId={}", row.securityId());
            }
        }
        if (unique.isEmpty()) {
            throw new DataImportException("Composition is empty");
        }
        indexCompositionRepository.deleteByIndexCodeAndReviewPeriod(indexCode.toUpperCase(), reviewPeriod);
        indexCompositionRepository.flush();
        List<IndexCompositionEntity> entities = unique.stream()
                .map(id -> new IndexCompositionEntity(indexCode.toUpperCase(), reviewPeriod, id.value()))
                .toList();
        indexCompositionRepository.saveAll(entities);
        saveSecurities(new ArrayList<>(unique));
        log.info("Imported composition indexCode={} reviewPeriod={} members={}", indexCode, reviewPeriod, entities.size());
        return new ImportResult("COMPOSITION", rows.size(), entities.size(), rows.size() - entities.size());
    }

    @Transactional
    public ImportResult importComposition(MultipartFile file) {
        var definition = indexDefinitionProvider.get("SMI", "Q3-2026");
        return self.importComposition(file, definition.indexCode().value(), definition.reviewPeriod());
    }

    @Transactional
    public AllImportResult importAll(MultipartFile spiUniverse, MultipartFile securityData, MultipartFile composition) {
        return new AllImportResult(self.importSpiUniverse(spiUniverse), self.importSecurityData(securityData),
                self.importComposition(composition));
    }

    private void saveSecurities(List<SecurityId> ids) {
        Set<Integer> existing = new LinkedHashSet<>(securityRepository.findAllById(ids.stream().map(SecurityId::value).toList())
                .stream().map(SecurityEntity::getId).toList());
        List<SecurityEntity> missing = ids.stream().map(SecurityId::value).distinct()
                .filter(id -> !existing.contains(id)).map(SecurityEntity::new).toList();
        if (!missing.isEmpty()) {
            securityRepository.saveAll(missing);
        }
    }

    private boolean sameMarketData(SecurityDataRow left, SecurityDataRow right) {
        return equalDecimal(left.price(), right.price()) && equalDecimal(left.shares(), right.shares())
                && equalDecimal(left.freeFloat(), right.freeFloat());
    }

    private boolean equalDecimal(BigDecimal left, BigDecimal right) {
        return left == null ? right == null : right != null && left.compareTo(right) == 0;
    }

    private <T> List<T> read(MultipartFile file, ReaderFunction<T> function) {
        if (file == null || file.isEmpty()) {
            throw new DataImportException("Uploaded file must not be empty");
        }
        try {
            return function.read(file.getInputStream(), file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        } catch (DataImportException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DataImportException("Could not import uploaded file", exception);
        }
    }

    @FunctionalInterface
    private interface ReaderFunction<T> {
        List<T> read(java.io.InputStream inputStream, String fileName);
    }

    public record ImportResult(String dataset, int inputRows, int storedRows, int deduplicatedRows) {
    }

    public record AllImportResult(ImportResult spiUniverse, ImportResult securityData, ImportResult composition) {
    }
}
