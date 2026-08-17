package uk.gov.hmcts.reform.civil.model.welshenhancements;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ChangeLanguagePreferenceUserType", generate = true)
public enum UserType {
    @CCD(label = "Claimant")
    CLAIMANT,
    @CCD(label = "Defendant")
    DEFENDANT
}
