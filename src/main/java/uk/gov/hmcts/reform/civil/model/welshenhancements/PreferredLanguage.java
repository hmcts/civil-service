package uk.gov.hmcts.reform.civil.model.welshenhancements;

public enum PreferredLanguage {
    ENGLISH,
    WELSH,
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
