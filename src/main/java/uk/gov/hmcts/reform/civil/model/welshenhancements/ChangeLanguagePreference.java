package uk.gov.hmcts.reform.civil.model.welshenhancements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeLanguagePreference {

    private UserType userType;
    private PreferredLanguage preferredLanguage;
}
