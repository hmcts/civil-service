package uk.gov.hmcts.reform.civil.enums;

public enum SettlementReason {

    JUDGE_ORDER("Settled following judge's order"),
    CONSENT_ORDER("Consent order approved");
    private final String label;

    SettlementReason(String value) {
        this.label = value;
    }
}
