package uk.gov.hmcts.reform.civil.model.welshenhancements;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChangeLanguagePreference {

    private UserType userType;
    private PreferredLanguage preferredLanguage;
}
