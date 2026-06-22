package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;

public class WaTaskUtil {

    private WaTaskUtil() {
        //no op
    }

    public static boolean confirmIfStateChangeRequired(CaseData caseData) {
        return !cancelApplicantWaDocumentUploadTask(caseData);
    }

    public static boolean cancelApplicantWaDocumentUploadTask(CaseData caseData) {
        ChangeLanguagePreference changeLanguagePreference = caseData.getChangeLanguagePreference();
        if (changeLanguagePreference == null || changeLanguagePreference.getPreferredLanguage() == null) {
            return false;
        }
        String preferredLanguage = changeLanguagePreference.getPreferredLanguage().name();
        return caseData.isLipvLipOneVOne() && PreferredLanguage.ENGLISH.name().equals(preferredLanguage);
    }
}
