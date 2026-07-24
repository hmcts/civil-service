package uk.gov.hmcts.reform.civil.logging;

import java.util.regex.Pattern;

public final class PiiRedactor {

    private static final String REDACTED = "[REDACTED]";
    private static final String SENSITIVE_FIELD_NAMES = String.join("|",
        "firstName",
        "lastName",
        "fullName",
        "partyName",
        "individualFirstName",
        "individualLastName",
        "soleTraderFirstName",
        "soleTraderLastName",
        "companyName",
        "organisationName",
        "email",
        "emailAddress",
        "partyEmail",
        "dateOfBirth",
        "individualDateOfBirth",
        "dob",
        "amount",
        "admittedAmount",
        "calculatedAmountInPence",
        "claimAmount",
        "claimFee",
        "claimFeeInPence",
        "defendantAdmittedAmount",
        "feeAmount",
        "instalmentAmount",
        "interest",
        "interestAmount",
        "outstandingAmount",
        "paidAmount",
        "partialAmount",
        "paymentAmount",
        "paymentReference",
        "paymentDate",
        "repaymentAmount",
        "totalClaimAmount",
        "address",
        "primaryAddress",
        "addressLine[1-3]?",
        "postCode",
        "postTown",
        "county",
        "country"
    );
    private static final Pattern EMAIL = Pattern.compile(
        "(?i)(?<![\\w.+-])[\\w.+-]+@[\\w.-]+\\.[a-z]{2,}(?![\\w.-])"
    );
    private static final Pattern SENSITIVE_FIELD = Pattern.compile(
        "(?i)(?<![\\w])(\\\"?(?:" + SENSITIVE_FIELD_NAMES + ")"
            + "\\\"?\\s*[:=]\\s*)(\\\"[^\\\"]*\\\"|[^,})\\r\\n]+)"
    );

    private PiiRedactor() {
    }

    public static String redact(String message) {
        if (message == null) {
            return null;
        }
        String fieldRedacted = SENSITIVE_FIELD.matcher(message).replaceAll("$1" + REDACTED);
        return EMAIL.matcher(fieldRedacted).replaceAll(REDACTED);
    }
}
