package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;

public class LanguageUtils {

    private LanguageUtils() {
        //NO-OP
    }

    public static Language determineLanguageForBulkPrint(CaseData caseData, boolean isClaimant, boolean welshEnabled) {
        if (!welshEnabled) {
            return Language.ENGLISH;
        }
        if (isClaimant) {
            if (caseData.getApplicant1DQ() != null
                && caseData.getApplicant1DQ().getApplicant1DQLanguage() != null
                && (caseData.getApplicant1DQ().getApplicant1DQLanguage().getDocuments() != null)) {
                return caseData.getApplicant1DQ().getApplicant1DQLanguage().getDocuments();
            } else {
                return switch (caseData.getClaimantBilingualLanguagePreference()) {
                    case "WELSH" -> Language.WELSH;
                    case "BOTH" -> Language.BOTH;
                    default -> Language.ENGLISH;
                };
            }
        } else {
            if (caseData.getRespondent1DQ() != null
                && caseData.getRespondent1DQ().getRespondent1DQLanguage() != null
                && (caseData.getRespondent1DQ().getRespondent1DQLanguage().getDocuments() != null)) {
                return caseData.getRespondent1DQ().getRespondent1DQLanguage().getDocuments();
            } else {
                String preference = caseData.getDefendantBilingualLanguagePreference();

                if (preference == null) {
                    return Language.ENGLISH;
                }

                return switch (preference) {
                    case "WELSH" -> Language.WELSH;
                    case "BOTH" -> Language.BOTH;
                    default -> Language.ENGLISH;
                };
            }
        }
    }
}
