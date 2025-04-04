package uk.gov.hmcts.reform.civil.model.welshenhancements;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ChangeLanguagePreference {

    private UserType userType;
    private PreferredLanguage preferredLanguage;
}
