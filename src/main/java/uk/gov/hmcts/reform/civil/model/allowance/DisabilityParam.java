package uk.gov.hmcts.reform.civil.model.allowance;

public record DisabilityParam(boolean disabled, boolean hasPartner, boolean severelyDisabled,  boolean dependant, boolean carer) {

    public boolean disabledSingle() {
        return disabled && !hasPartner && !severelyDisabled;
    }
    public boolean disabledCouple() {
        return disabled && hasPartner && !severelyDisabled;
    }
    public boolean disabledSeverelySingle() {
        return !hasPartner && severelyDisabled;
    }
    public boolean disabledSeverelyCouple() {
        return hasPartner && severelyDisabled;
    }

    public boolean disabledDependant() {
        return dependant;
    }

    public boolean isCarer() {
        return carer;
    }

}
