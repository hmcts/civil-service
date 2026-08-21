package uk.gov.hmcts.reform.civil.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum PersonalInjuryType {
    @CCD(label = "Road accident")
    ROAD_ACCIDENT,
    @CCD(label = "Work accident")
    WORK_ACCIDENT,
    @CCD(label = "Public liability accident")
    PUBLIC_LIABILITY,
    @CCD(label = "Holiday illness")
    HOLIDAY_ILLNESS,
    @CCD(label = "Disease claim")
    DISEASE_CLAIM,
    @CCD(label = "Noise induced hearing loss")
    NOISE_INDUCED_HEARING_LOSS,
    @CCD(label = "Personal Injury - other")
    PERSONAL_INJURY_OTHER
}
