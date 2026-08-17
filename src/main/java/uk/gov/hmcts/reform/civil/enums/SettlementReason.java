package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SettleReason", generate = true)
public enum SettlementReason {

    @CCD(label = "Settled following judge's order")
    JUDGE_ORDER("Settled following judge's order"),
    @CCD(label = "Consent order approved")
    CONSENT_ORDER("Consent order approved");
    private final String label;

    SettlementReason(String value) {
        this.label = value;
    }
}
