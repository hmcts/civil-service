package uk.gov.hmcts.reform.civil.exceptions;

public class HearingLocationNotFoundException extends IllegalArgumentException {

    public HearingLocationNotFoundException(String hearingId, String venueId) {
        super("Hearing location data not available for hearing " + hearingId + " venueId " + venueId);
    }
}
