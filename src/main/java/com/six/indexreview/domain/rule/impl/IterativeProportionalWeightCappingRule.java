package com.six.indexreview.domain.rule.impl;

import com.six.indexreview.application.exception.ReviewValidationException;
import com.six.indexreview.domain.engine.IndexReviewContext;
import com.six.indexreview.domain.model.SelectedConstituent;
import com.six.indexreview.domain.rule.WeightCappingRule;
import com.six.indexreview.domain.service.PrecisionPolicy;
import com.six.indexreview.domain.service.WeightCapper;
import com.six.indexreview.validation.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.Consumer;

/**
 * Caps constituent weights and redistributes excess proportionally.
 *
 * BigDecimal and a high internal scale are used to keep redistribution
 * deterministic and to ensure the published output sums to exactly 1.0.
 */
/**
 * @author Milos Davitkovic
 */
@Slf4j
@Component
public class IterativeProportionalWeightCappingRule implements WeightCappingRule, WeightCapper {
    private static final BigDecimal ONE = BigDecimal.ONE;
    private final PrecisionPolicy precisionPolicy;

    public IterativeProportionalWeightCappingRule() {
        this(new PrecisionPolicy(16, 10, java.math.RoundingMode.HALF_UP));
    }

    @Autowired
    public IterativeProportionalWeightCappingRule(PrecisionPolicy precisionPolicy) {
        this.precisionPolicy = precisionPolicy;
    }

    @Override
    public String code() {
        return "APPLY_WEIGHT_CAP";
    }

    @Override
    public String description() {
        return "Iteratively cap weights and redistribute excess proportionally to uncapped constituents";
    }

    @Override
    public IndexReviewContext apply(IndexReviewContext context) {
        log.info("Starting rule {} maxWeight={} selected={}", code(), context.definition().maxWeight(),
                context.selectedConstituents().size());
        List<String> redistributionMessages = new ArrayList<>();
        List<SelectedConstituent> capped;
        try {
            // Store redistribution steps separately so the audit trail can show
            // how excess weight moved between constituents.
            capped = capInternal(context.selectedConstituents(), context.definition().maxWeight(),
                    redistributionMessages::add);
        } catch (IllegalArgumentException exception) {
            throw new ReviewValidationException("Weight capping validation failed", List.of(
                    ValidationError.error("WEIGHT_CAPPING_FAILED", "finalWeights", exception.getMessage())));
        }
        context.replaceSelected(capped);
        for (String message : redistributionMessages) {
            context.audit(code(), message, null, null);
        }
        capped.forEach(value -> {
            if (value.capped()) {
                context.audit(code(), value.securityId(), "Constituent weight capped at configured maximum.",
                        value.rawWeight().toPlainString(), value.finalWeight().toPlainString());
            }
            context.audit(code(), value.securityId(), "Final weight and capping factor assigned.",
                    "rawWeight=" + value.rawWeight(),
                    "finalWeight=" + value.finalWeight() + ",cappingFactor=" + value.cappingFactor());
        });
        log.info("Completed rule {} finalWeightSum={}", code(), capped.stream()
                .map(SelectedConstituent::finalWeight).reduce(BigDecimal.ZERO, BigDecimal::add));
        return context;
    }

    @Override
    public List<SelectedConstituent> cap(List<SelectedConstituent> constituents, BigDecimal maxWeight) {
        return capInternal(constituents, maxWeight, ignored -> {
        });
    }

    private List<SelectedConstituent> capInternal(List<SelectedConstituent> constituents,
                                                  BigDecimal maxWeight,
                                                  Consumer<String> redistributionAudit) {
        validateInput(constituents, maxWeight);
        // Use a wider working precision than the published output scale so
        // repeated redistribution does not create non-deterministic rounding
        // artifacts.
        var mathContext = precisionPolicy.internalMathContext();
        Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> working = initialWeights(constituents);
        adjustToTarget(working, maxWeight, Set.of(), mathContext);

        Set<com.six.indexreview.domain.model.SecurityId> cappedIds = new HashSet<>();
        redistribute(constituents, maxWeight, working, cappedIds, mathContext, redistributionAudit);

        adjustToTarget(working, maxWeight, cappedIds, mathContext);
        Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> output = outputWeights(constituents, working);
        adjustOutputResidual(output, constituents, maxWeight, cappedIds);
        validateOutput(output, maxWeight);
        return toResults(constituents, output, cappedIds);
    }

    private void validateInput(List<SelectedConstituent> constituents, BigDecimal maxWeight) {
        if (constituents == null || constituents.isEmpty()) throw new IllegalArgumentException("At least one constituent is required for capping");
        if (maxWeight == null || maxWeight.compareTo(BigDecimal.ZERO) <= 0 || maxWeight.compareTo(ONE) > 0) throw new IllegalArgumentException("Maximum weight must be between 0 and 1");
        if (maxWeight.multiply(BigDecimal.valueOf(constituents.size())).compareTo(ONE) < 0) throw new IllegalArgumentException("Weight cap is mathematically impossible for the selected count");
        Set<com.six.indexreview.domain.model.SecurityId> ids = new HashSet<>();
        for (SelectedConstituent constituent : constituents) {
            if (!ids.add(constituent.securityId())) {
                throw new IllegalArgumentException("Duplicate security id: " + constituent.securityId());
            }
            if (constituent.rawWeight() == null || constituent.rawWeight().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Raw weight must be positive for " + constituent.securityId());
            }
        }
    }

    private Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> initialWeights(List<SelectedConstituent> constituents) {
        Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> weights = new LinkedHashMap<>();
        constituents.forEach(value -> weights.put(value.securityId(), value.rawWeight()));
        return weights;
    }

    private void redistribute(List<SelectedConstituent> constituents, BigDecimal maxWeight,
                              Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> working,
                              Set<com.six.indexreview.domain.model.SecurityId> cappedIds,
                              MathContext mathContext, Consumer<String> audit) {
        for (int iteration = 1; ; iteration++) {
            if (iteration > constituents.size() + 2) throw new IllegalStateException("Weight capping did not converge");
            List<SelectedConstituent> overCap = constituents.stream()
                    .filter(value -> !cappedIds.contains(value.securityId()))
                    .filter(value -> working.get(value.securityId()).compareTo(maxWeight) > 0).toList();
            if (overCap.isEmpty()) return;
            BigDecimal excess = capAndMeasureExcess(overCap, maxWeight, working, cappedIds, mathContext);
            List<SelectedConstituent> uncapped = constituents.stream()
                    .filter(value -> !cappedIds.contains(value.securityId())).toList();
            // Redistribute excess proportionally to the remaining uncapped
            // weights so the relative order of the survivors remains stable.
            redistributeToUncapped(uncapped, excess, working, mathContext);
            String message = "Redistribution iteration " + iteration + " capped="
                    + overCap.stream().map(value -> value.securityId().toString()).sorted().toList()
                    + " excess=" + excess.toPlainString();
            log.debug(message);
            audit.accept(message);
        }
    }

    private BigDecimal capAndMeasureExcess(List<SelectedConstituent> overCap, BigDecimal maxWeight,
                                           Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> working,
                                           Set<com.six.indexreview.domain.model.SecurityId> cappedIds,
                                           MathContext mathContext) {
        BigDecimal excess = BigDecimal.ZERO;
        for (SelectedConstituent constituent : overCap) {
            BigDecimal value = working.get(constituent.securityId());
            excess = excess.add(value.subtract(maxWeight), mathContext);
            working.put(constituent.securityId(), maxWeight);
            cappedIds.add(constituent.securityId());
        }
        return excess;
    }

    private void redistributeToUncapped(List<SelectedConstituent> uncapped, BigDecimal excess,
                                       Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> working,
                                       MathContext mathContext) {
        if (uncapped.isEmpty()) throw new IllegalArgumentException("Cannot redistribute excess: all constituents are capped");
        BigDecimal total = uncapped.stream().map(value -> working.get(value.securityId()))
                .reduce(BigDecimal.ZERO, (left, right) -> left.add(right, mathContext));
        if (total.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Cannot redistribute excess to zero-weight constituents");
        uncapped.forEach(value -> {
            working.compute(value.securityId(), (key, current) -> current.add(
                    excess.multiply(current, mathContext).divide(total, mathContext), mathContext));
        });
    }

    private Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> outputWeights(List<SelectedConstituent> constituents,
                                                                                        Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> working) {
        Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> output = new LinkedHashMap<>();
        constituents.forEach(value -> output.put(value.securityId(), precisionPolicy.output(working.get(value.securityId()))));
        return output;
    }

    private void validateOutput(Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> output, BigDecimal maxWeight) {
        BigDecimal expected = precisionPolicy.output(ONE);
        BigDecimal sum = output.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(expected) != 0) throw new IllegalArgumentException("Final weights do not sum to " + expected + ": " + sum);
        for (BigDecimal value : output.values()) {
            if (value.compareTo(maxWeight) > 0) {
                throw new IllegalArgumentException("Final weight exceeds configured cap after rounding: " + value);
            }
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Final weight cannot be negative: " + value);
            }
        }
    }

    private List<SelectedConstituent> toResults(List<SelectedConstituent> constituents,
                                                  Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> output,
                                                  Set<com.six.indexreview.domain.model.SecurityId> cappedIds) {
        List<SelectedConstituent> result = new ArrayList<>(constituents.size());
        for (SelectedConstituent constituent : constituents) {
            BigDecimal finalWeight = output.get(constituent.securityId());
            BigDecimal factor = finalWeight.divide(constituent.rawWeight(), precisionPolicy.outputScale(), precisionPolicy.roundingMode());
            result.add(constituent.withFinalWeight(finalWeight, factor, cappedIds.contains(constituent.securityId())));
        }
        return result;
    }

    private void adjustToTarget(Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> values,
                                BigDecimal maxWeight,
                                Set<com.six.indexreview.domain.model.SecurityId> cappedIds,
                                MathContext mathContext) {
        BigDecimal sum = values.values().stream().reduce(BigDecimal.ZERO, (left, right) -> left.add(right, mathContext));
        BigDecimal residual = ONE.subtract(sum, mathContext);
        if (residual.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        com.six.indexreview.domain.model.SecurityId target = chooseResidualTarget(values, maxWeight, cappedIds, residual);
        values.put(target, values.get(target).add(residual, mathContext));
    }

    private com.six.indexreview.domain.model.SecurityId chooseResidualTarget(
            Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> values,
            BigDecimal maxWeight,
            Set<com.six.indexreview.domain.model.SecurityId> cappedIds,
            BigDecimal residual) {
        Comparator<com.six.indexreview.domain.model.SecurityId> byLargest = Comparator
                .<com.six.indexreview.domain.model.SecurityId, BigDecimal>comparing(values::get).reversed()
                .thenComparing(com.six.indexreview.domain.model.SecurityId::compareTo);
        List<com.six.indexreview.domain.model.SecurityId> preferred = values.keySet().stream()
                .filter(id -> !cappedIds.contains(id)).sorted(byLargest).toList();
        List<com.six.indexreview.domain.model.SecurityId> fallback = values.keySet().stream().sorted(byLargest).toList();
        for (com.six.indexreview.domain.model.SecurityId id : concat(preferred, fallback)) {
            BigDecimal candidate = values.get(id).add(residual);
            if (candidate.compareTo(BigDecimal.ZERO) >= 0 && candidate.compareTo(maxWeight) <= 0) {
                return id;
            }
        }
        throw new IllegalArgumentException("Unable to assign deterministic weight residual without violating cap");
    }

    private List<com.six.indexreview.domain.model.SecurityId> concat(
            List<com.six.indexreview.domain.model.SecurityId> first,
            List<com.six.indexreview.domain.model.SecurityId> second) {
        List<com.six.indexreview.domain.model.SecurityId> result = new ArrayList<>(first);
        second.stream().filter(id -> !result.contains(id)).forEach(result::add);
        return result;
    }

    private void adjustOutputResidual(Map<com.six.indexreview.domain.model.SecurityId, BigDecimal> output,
                                      List<SelectedConstituent> constituents,
                                      BigDecimal maxWeight,
                                      Set<com.six.indexreview.domain.model.SecurityId> cappedIds) {
        BigDecimal expected = precisionPolicy.output(ONE);
        BigDecimal sum = output.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal residual = expected.subtract(sum);
        if (residual.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        Comparator<SelectedConstituent> largest = Comparator.comparing((SelectedConstituent value) -> output.get(value.securityId()))
                .reversed().thenComparing(SelectedConstituent::securityId);
        List<SelectedConstituent> ordered = new ArrayList<>(constituents);
        ordered.sort(largest);
        List<SelectedConstituent> preferred = ordered.stream().filter(value -> !cappedIds.contains(value.securityId())).toList();
        List<SelectedConstituent> fallback = ordered.stream().filter(value -> cappedIds.contains(value.securityId())).toList();
        for (SelectedConstituent value : concatConstituents(preferred, fallback)) {
            BigDecimal candidate = output.get(value.securityId()).add(residual);
            if (candidate.compareTo(BigDecimal.ZERO) >= 0 && candidate.compareTo(maxWeight) <= 0) {
                output.put(value.securityId(), candidate);
                return;
            }
        }
        throw new IllegalArgumentException("Unable to assign output rounding residual without violating cap");
    }

    private List<SelectedConstituent> concatConstituents(List<SelectedConstituent> first,
                                                         List<SelectedConstituent> second) {
        List<SelectedConstituent> result = new ArrayList<>(first);
        second.stream().filter(value -> !result.contains(value)).forEach(result::add);
        return result;
    }
}
