package uk.gov.hmcts.reform.hearings.hearingnotice.exception;

public class GetHearingException extends Exception {
    public static final String MESSAGE_TEMPLATE = "Failed to retrieve hearing with Id: %s from HMC";

    public GetHearingException(String hearingId, Throwable t) {
        super(String.format(MESSAGE_TEMPLATE, hearingId), t);
    }

    public GetHearingException(String hearingId) {
        super(String.format(MESSAGE_TEMPLATE, hearingId));
    }
}
