package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum ReasonForProceedingOnPaper {
    @CCD(label = "Application")
    APPLICATION,
    @CCD(label = "Judgment request")
    JUDGEMENT_REQUEST,
    DEFENDANT_DOES_NOT_CONSENT,
    @CCD(label = "Case settled")
    CASE_SETTLED,
    @CCD(label = "Other")
    OTHER;
}
