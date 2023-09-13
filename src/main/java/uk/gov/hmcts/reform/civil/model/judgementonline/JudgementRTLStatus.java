package uk.gov.hmcts.reform.civil.model.judgementonline;

public enum JudgementRTLStatus {
    REGISTRATION("R"),
    Cancellation("C"),
    Satisfaction("S"),
    Modified("M");

    private String rtlState;

    JudgementRTLStatus(String rtlState) {
        this.rtlState = rtlState;
    }
}
