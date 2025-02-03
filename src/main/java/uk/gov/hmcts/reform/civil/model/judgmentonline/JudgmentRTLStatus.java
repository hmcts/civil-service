package uk.gov.hmcts.reform.civil.model.judgmentonline;

public enum JudgmentRTLStatus {
    ISSUED("R"),
    CANCELLED("C"),
    SET_ASIDE("C"),
    SATISFIED("S"),
    MODIFIED_EXISTING("M"),
    MODIFIED_RTL_STATE("R");

    private String rtlState;

    JudgmentRTLStatus(String rtlState) {
        this.rtlState = rtlState;
    }

    public String getRtlState() {
        return rtlState;
    }
}
