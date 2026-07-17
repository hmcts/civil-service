package uk.gov.hmcts.reform.civil.model.welshenhancements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeLanguagePreference {

    @CCD(label = "Which user does this change apply to?", searchable = false)
    private UserType userType;
    @CCD(label = "What is their preferred language?", searchable = false)
    private PreferredLanguage preferredLanguage;
}
