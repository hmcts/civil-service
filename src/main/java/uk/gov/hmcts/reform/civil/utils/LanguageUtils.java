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
        String languageString = isClaimant ? caseData.getClaimantBilingualLanguagePreference() : caseData.getDefendantBilingualLanguagePreference();
        return switch (languageString) {
            case "WELSH" -> Language.WELSH;
            case "BOTH" -> Language.BOTH;
            default -> Language.ENGLISH;
        };
    }
}
