package uk.gov.hmcts.reform.civil.model.allowance;

public record DisabilityParam(boolean disabled, boolean hasPartner, boolean severelyDisabled,  boolean dependant) {

    public boolean disabledSingle() {
        return disabled && !hasPartner;
    }
    public boolean disabledCouple() {
        return disabled && hasPartner;
    }
    public boolean disabledSeverlySingle() {
        return disabledSingle() && severelyDisabled;
    }
    public boolean disabledSeverlyCouple() {
        return disabledCouple() && severelyDisabled;
    }

    public boolean disabledDependant() {
        return dependant;
    }

}
