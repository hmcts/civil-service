package uk.gov.hmcts.reform.civil.model.welshenhancements;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum PreferredLanguage {
    @CCD(label = "English")
    ENGLISH,
    @CCD(label = "Welsh")
    WELSH,
    @CCD(label = "English and Welsh")
    ENGLISH_AND_WELSH;

    public static PreferredLanguage fromString(String languageString) {
        if (languageString == null) {
            languageString = "";
        }
        return switch (languageString) {
            case "WELSH" -> PreferredLanguage.WELSH;
            case "BOTH" -> PreferredLanguage.ENGLISH_AND_WELSH;
            default -> PreferredLanguage.ENGLISH;
        };
    }
}
