package uk.gov.hmcts.reform.civil.utils;

public class HearingReferenceNumber {

    private static String hearingReference = "000000";

    private HearingReferenceNumber() {
    }

    public static String generateHearingReference() {
        int hearingCount = Integer.parseInt(hearingReference);
        hearingCount++;
        hearingReference = String.format("%06d", hearingCount);
        StringBuilder referenceNumber = new StringBuilder(hearingReference);
        referenceNumber.insert(3, "HN");

        return referenceNumber.toString();
    }

}

