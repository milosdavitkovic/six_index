package com.six.indexreview.application;

import com.six.indexreview.api.dto.AuditEventResponse;
import com.six.indexreview.api.dto.ReviewResponse;
import com.six.indexreview.application.exception.ResourceNotFoundException;
import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.engine.IndexReviewEngine;
import com.six.indexreview.domain.model.IndexDefinition;
import com.six.indexreview.domain.model.ReviewResult;
import com.six.indexreview.domain.service.PrecisionPolicy;
import com.six.indexreview.infrastructure.config.IndexDefinitionProvider;
import com.six.indexreview.infrastructure.persistence.ReviewDataLoader;
import com.six.indexreview.infrastructure.persistence.entity.ReviewResultEntity;
import com.six.indexreview.infrastructure.persistence.mapper.ReviewResultMapper;
import com.six.indexreview.infrastructure.persistence.repository.AuditEventRepository;
import com.six.indexreview.infrastructure.persistence.repository.ReviewResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Orchestrates review execution, persistence, and response assembly.
 *
 * The service stays outside the domain rules so methodology changes can be
 * introduced in the engine without affecting HTTP or persistence concerns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexReviewService {
    private final IndexDefinitionProvider indexDefinitionProvider;
    private final ReviewDataLoader reviewDataLoader;
    private final IndexReviewEngine reviewEngine;
    private final PrecisionPolicy precisionPolicy;
    private final ReviewResultRepository reviewResultRepository;
    private final ReviewResultMapper reviewResultMapper;
    private final ReviewResultAssembler reviewResultAssembler;
    private final AuditEventRepository auditEventRepository;
    private final Clock reviewClock;

    @Transactional
    public ReviewResponse run(String indexCode, String reviewPeriod) {
        Objects.requireNonNull(indexCode, "indexCode must not be null");
        Objects.requireNonNull(reviewPeriod, "reviewPeriod must not be null");
        IndexDefinition definition = indexDefinitionProvider.get(indexCode, reviewPeriod);
        log.info("Index review started indexCode={} reviewPeriod={} cutOffDate={} reviewDate={}",
                definition.indexCode(), definition.reviewPeriod(), definition.reviewDates().cutOffDate(),
                definition.reviewDates().reviewDate());
        // Load a complete snapshot before execution so the review uses a fixed
        // input set and remains auditable after persistence.
        var snapshot = reviewDataLoader.load(definition.indexCode().value(), definition.reviewPeriod(),
                definition.reviewDates().cutOffDate(), definition.reviewDates().reviewDate());
        log.info("Loaded review inputs universe={} cutOffMarketData={} reviewMarketData={} currentComposition={}",
                snapshot.universe().size(), snapshot.cutOffMarketData().size(), snapshot.reviewMarketData().size(),
                snapshot.currentComposition().size());
        IndexReviewContext context = new IndexReviewContext(definition, snapshot, precisionPolicy, Instant.now(reviewClock));
        reviewEngine.execute(context);
        // Persist the full result after all rules have completed so the stored
        // record reflects the exact review state that was audited.
        ReviewResultEntity saved = reviewResultRepository.save(reviewResultMapper.toEntity(context));
        ReviewResult result = reviewResultMapper.toDomain(saved);
        log.info("Index review completed reviewResultId={} eligible={} selected={} joiners={} leavers={}",
                saved.getId(), result.totalEligibleSecurities(), result.totalSelectedConstituents(),
                result.decisions().stream().filter(value -> value.decisionType().name().equals("JOINER")).count(),
                result.decisions().stream().filter(value -> value.decisionType().name().equals("LEAVER")).count());
        return reviewResultAssembler.toResponse(result);
    }

    @Transactional(readOnly = true)
    public ReviewResponse latest(String indexCode, String reviewPeriod) {
        Objects.requireNonNull(indexCode, "indexCode must not be null");
        Objects.requireNonNull(reviewPeriod, "reviewPeriod must not be null");
        ReviewResultEntity entity = reviewResultRepository
                .findTopByIndexCodeAndReviewPeriodOrderByCreatedAtDescIdDesc(indexCode.toUpperCase(), reviewPeriod)
                .orElseThrow(() -> new ResourceNotFoundException("No review result found for " + indexCode + "/" + reviewPeriod));
        return reviewResultAssembler.toResponse(reviewResultMapper.toDomain(entity));
    }

    @Transactional(readOnly = true)
    public ReviewResponse byId(Long id) {
        Objects.requireNonNull(id, "id must not be null");
        ReviewResultEntity entity = reviewResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review result not found: " + id));
        return reviewResultAssembler.toResponse(reviewResultMapper.toDomain(entity));
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> auditForSecurity(Long reviewResultId, int securityId) {
        Objects.requireNonNull(reviewResultId, "reviewResultId must not be null");
        reviewResultRepository.findById(reviewResultId)
                .orElseThrow(() -> new ResourceNotFoundException("Review result not found: " + reviewResultId));
        // Audit events are returned in execution order to explain the inclusion
        // or exclusion path for a single security.
        return auditEventRepository.findAllByReviewResult_IdAndSecurityIdOrderBySequenceNumberAsc(reviewResultId, securityId)
                .stream().map(value -> new AuditEventResponse(value.getTimestamp(), value.getRuleCode(), value.getSecurityId(),
                        value.getMessage(), value.getInputValue(), value.getOutputValue())).toList();
    }
}
