package com.six.indexreview.domain.engine;

import com.six.indexreview.domain.model.*;
import com.six.indexreview.domain.service.PrecisionPolicy;
import com.six.indexreview.validation.ValidationError;

import java.time.Instant;
import java.util.*;

/** Mutable execution state owned by one review run; rules only communicate through this object. */
public final class IndexReviewContext {
    private final IndexDefinition definition;
    private final Set<SecurityId> universe;
    private final Map<SecurityId, com.six.indexreview.domain.model.MarketData> cutOffMarketData;
    private final Map<SecurityId, com.six.indexreview.domain.model.MarketData> reviewMarketData;
    private final Set<SecurityId> currentComposition;
    private final PrecisionPolicy precisionPolicy;
    private final Instant executionTimestamp;
    private final List<EligibleSecurity> eligibleSecurities = new ArrayList<>();
    private final List<RejectedSecurity> rejectedSecurities = new ArrayList<>();
    private final List<RankedSecurity> rankedSecurities = new ArrayList<>();
    private final List<SelectedConstituent> selectedConstituents = new ArrayList<>();
    private final List<ReviewDecision> decisions = new ArrayList<>();
    private final List<AuditEvent> auditEvents = new ArrayList<>();
    private final List<ValidationError> validationErrors = new ArrayList<>();
    private final List<ValidationError> validationWarnings = new ArrayList<>();

    public IndexReviewContext(IndexDefinition definition, ReviewDataSnapshot snapshot,
                              PrecisionPolicy precisionPolicy, Instant executionTimestamp) {
        this.definition = definition;
        this.universe = Collections.unmodifiableSet(new TreeSet<>(snapshot.universe()));
        this.cutOffMarketData = Collections.unmodifiableMap(new LinkedHashMap<>(snapshot.cutOffMarketData()));
        this.reviewMarketData = Collections.unmodifiableMap(new LinkedHashMap<>(snapshot.reviewMarketData()));
        this.currentComposition = Collections.unmodifiableSet(new LinkedHashSet<>(snapshot.currentComposition()));
        this.precisionPolicy = precisionPolicy;
        this.executionTimestamp = executionTimestamp;
    }

    public IndexDefinition definition() {
        return definition;
    }

    public Set<SecurityId> universe() {
        return universe;
    }

    public Map<SecurityId, com.six.indexreview.domain.model.MarketData> cutOffMarketData() {
        return cutOffMarketData;
    }

    public Map<SecurityId, com.six.indexreview.domain.model.MarketData> reviewMarketData() {
        return reviewMarketData;
    }

    public Set<SecurityId> currentComposition() {
        return currentComposition;
    }

    public PrecisionPolicy precisionPolicy() {
        return precisionPolicy;
    }

    public Instant executionTimestamp() {
        return executionTimestamp;
    }

    public List<EligibleSecurity> eligibleSecurities() {
        return eligibleSecurities;
    }

    public List<RejectedSecurity> rejectedSecurities() {
        return rejectedSecurities;
    }

    public List<RankedSecurity> rankedSecurities() {
        return rankedSecurities;
    }

    public List<SelectedConstituent> selectedConstituents() {
        return selectedConstituents;
    }

    public List<ReviewDecision> decisions() {
        return decisions;
    }

    public List<AuditEvent> auditEvents() {
        return auditEvents;
    }

    public List<ValidationError> validationErrors() {
        return validationErrors;
    }

    public List<ValidationError> validationWarnings() {
        return validationWarnings;
    }

    public void replaceEligible(List<EligibleSecurity> values) {
        eligibleSecurities.clear();
        eligibleSecurities.addAll(values);
    }

    public void addRejected(RejectedSecurity value) {
        rejectedSecurities.add(value);
    }

    public void replaceRanked(List<RankedSecurity> values) {
        rankedSecurities.clear();
        rankedSecurities.addAll(values);
    }

    public void replaceSelected(List<SelectedConstituent> values) {
        selectedConstituents.clear();
        selectedConstituents.addAll(values);
    }

    public void replaceDecisions(List<ReviewDecision> values) {
        decisions.clear();
        decisions.addAll(values);
    }

    public void addValidationErrors(List<ValidationError> values) {
        validationErrors.addAll(values);
    }

    public void addValidationWarning(ValidationError value) {
        validationWarnings.add(value);
    }

    public void audit(String ruleCode, SecurityId securityId, String message,
                      String inputValue, String outputValue) {
        auditEvents.add(new AuditEvent(executionTimestamp, ruleCode, securityId, message,
                inputValue, outputValue));
    }

    public void audit(String ruleCode, String message, String inputValue, String outputValue) {
        audit(ruleCode, null, message, inputValue, outputValue);
    }
}
