package uk.gov.hmcts.reform.civil.model.breathing;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum BreathingSpaceType {
    @CCD(label = "Mental Health Crises Moratorium")
    MENTAL_HEALTH,
    @CCD(label = "Standard Breathing Space")
    STANDARD
}
