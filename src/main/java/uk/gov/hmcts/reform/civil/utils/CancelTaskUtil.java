package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;

public class CancelTaskUtil {

    private CancelTaskUtil() {
        //no op
    }

    public static boolean cancelApplicantWaDocumentUploadTask(CaseData data) {
        ChangeLanguagePreference changeLanguagePreference = data.getChangeLanguagePreference();
        if (changeLanguagePreference == null || changeLanguagePreference.getPreferredLanguage() == null) {
            return false;
        }
        String preferredLanguage = changeLanguagePreference.getPreferredLanguage().name();
        return data.isLipvLipOneVOne() && PreferredLanguage.ENGLISH.name().equals(preferredLanguage);
    }
}
