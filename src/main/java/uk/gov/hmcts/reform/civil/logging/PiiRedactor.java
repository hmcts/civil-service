package uk.gov.hmcts.reform.civil.logging;

import java.util.regex.Pattern;

public final class PiiRedactor {

    private static final String REDACTED = "[REDACTED]";
    private static final Pattern EMAIL = Pattern.compile(
        "(?i)(?<![\\w.+-])[\\w.+-]+@[\\w.-]+\\.[a-z]{2,}(?![\\w.-])"
    );
    private static final Pattern PII_FIELD = Pattern.compile(
        "(?i)(\\\"?(?:firstName|lastName|fullName|partyName|individualFirstName|individualLastName|"
            + "email|emailAddress|dateOfBirth|dob|addressLine[1-3]?|postCode|postcode|postTown|county|country)"
            + "\\\"?\\s*[:=]\\s*)(\\\"[^\\\"]*\\\"|[^,})]+)"
    );

    private PiiRedactor() {
    }

    public static String redact(String message) {
        if (message == null) {
            return null;
        }
        String fieldRedacted = PII_FIELD.matcher(message).replaceAll("$1" + REDACTED);
        return EMAIL.matcher(fieldRedacted).replaceAll(REDACTED);
    }
}
