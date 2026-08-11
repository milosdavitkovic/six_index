package com.six.indexreview.domain.engine;

import com.six.indexreview.application.exception.ReviewExecutionException;
import com.six.indexreview.domain.rule.BufferRule;
import com.six.indexreview.domain.rule.ReviewRule;
import com.six.indexreview.domain.rule.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class IndexReviewEngine {
    private final DefaultDataValidationRule validationRule;
    private final SpiUniverseEligibilityRule eligibilityRule;
    private final FfmcapRankingRule rankingRule;
    private final TopNSelectionRule selectionRule;
    private final BufferRule noOpBufferRule;
    private final BufferRule configurableBufferRule;
    private final DefaultJoinerLeaverRule joinerLeaverRule;
    private final FfmcapWeightCalculationRule weightCalculationRule;
    private final IterativeProportionalWeightCappingRule weightCappingRule;
    private final DefaultAuditReportRule auditReportRule;

    public IndexReviewEngine(DefaultDataValidationRule validationRule,
                             SpiUniverseEligibilityRule eligibilityRule,
                             FfmcapRankingRule rankingRule,
                             TopNSelectionRule selectionRule,
                             @Qualifier("noOpBufferRule") BufferRule noOpBufferRule,
                             @Qualifier("configurableBufferRule") BufferRule configurableBufferRule,
                             DefaultJoinerLeaverRule joinerLeaverRule,
                             FfmcapWeightCalculationRule weightCalculationRule,
                             IterativeProportionalWeightCappingRule weightCappingRule,
                             DefaultAuditReportRule auditReportRule) {
        this.validationRule = validationRule;
        this.eligibilityRule = eligibilityRule;
        this.rankingRule = rankingRule;
        this.selectionRule = selectionRule;
        this.noOpBufferRule = noOpBufferRule;
        this.configurableBufferRule = configurableBufferRule;
        this.joinerLeaverRule = joinerLeaverRule;
        this.weightCalculationRule = weightCalculationRule;
        this.weightCappingRule = weightCappingRule;
        this.auditReportRule = auditReportRule;
    }

    public IndexReviewContext execute(IndexReviewContext context) {
        List<ReviewRule> rules = List.of(validationRule, eligibilityRule, rankingRule, selectionRule,
                chooseBufferRule(context), joinerLeaverRule, weightCalculationRule, weightCappingRule,
                auditReportRule);
        log.info("Executing review rule pipeline indexCode={} reviewPeriod={} ruleCount={}",
                context.definition().indexCode(), context.definition().reviewPeriod(), rules.size());
        for (ReviewRule rule : rules) {
            try {
                rule.apply(context);
            } catch (RuntimeException exception) {
                log.error("Review rule failed ruleCode={} message={}", rule.code(), exception.getMessage(), exception);
                throw exception;
            }
        }
        return context;
    }

    private BufferRule chooseBufferRule(IndexReviewContext context) {
        return switch (context.definition().bufferRule()) {
            case "NONE", "NOOP", "NO_OP", "" -> noOpBufferRule;
            case "CONFIGURABLE", "TOP_N_WITH_BUFFER" -> configurableBufferRule;
            default -> throw new ReviewExecutionException("Unsupported buffer rule: " + context.definition().bufferRule());
        };
    }
}
