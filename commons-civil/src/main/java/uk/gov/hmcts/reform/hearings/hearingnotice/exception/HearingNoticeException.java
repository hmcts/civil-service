package uk.gov.hmcts.reform.hearings.hearingnotice.exception;

public class HearingNoticeException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Failed to retrieve data from HMC";

    public HearingNoticeException(Throwable cause) {
        super(MESSAGE_TEMPLATE, cause);
    }
}
