package uk.gov.hmcts.reform.civil.model.judgmentonline;

public enum JudgmentRTLStatus {
    REGISTRATION("R"),
    CANCELLATION("C"),
    SATISFACTION("S"),
    MODIFIED("M");

    private String rtlState;

    JudgmentRTLStatus(String rtlState) {
        this.rtlState = rtlState;
    }

    public String getRtlState() {
        return rtlState;
    }
}
