package uk.gov.hmcts.reform.civil.model.judgementonline;

public enum JudgmentRTLStatus {
    REGISTRATION("R"),
    Cancellation("C"),
    Satisfaction("S"),
    Modified("M");

    private String rtlState;

    JudgmentRTLStatus(String rtlState) {
        this.rtlState = rtlState;
    }
}
