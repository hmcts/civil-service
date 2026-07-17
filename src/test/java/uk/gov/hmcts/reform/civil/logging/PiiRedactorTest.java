package uk.gov.hmcts.reform.civil.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PiiRedactorTest {

    @Test
    void shouldRedactEmailAddresses() {
        assertThat(PiiRedactor.redact("Sending to jane.doe@example.com"))
            .isEqualTo("Sending to [REDACTED]");
    }

    @Test
    void shouldRedactJsonPiiFields() {
        String message = "{\"firstName\":\"Jane\",\"dateOfBirth\":\"1990-01-01\",\"postcode\":\"SW1A 1AA\"}";

        assertThat(PiiRedactor.redact(message))
            .doesNotContain("Jane", "1990-01-01", "SW1A 1AA")
            .contains("\"firstName\":[REDACTED]", "\"dateOfBirth\":[REDACTED]", "\"postcode\":[REDACTED]");
    }

    @Test
    void shouldRedactLombokObjectPiiFields() {
        String message = "Party(individualFirstName=Jane, individualLastName=Doe, addressLine1=1 High Street)";

        assertThat(PiiRedactor.redact(message))
            .doesNotContain("Jane", "Doe", "1 High Street")
            .contains("individualFirstName=[REDACTED]", "individualLastName=[REDACTED]", "addressLine1=[REDACTED]");
    }

    @Test
    void shouldRetainOperationalIdentifiersAndRedactFinancialValues() {
        String message = "caseId=1234567890123456, caseReference=ABC-123, userId=user-123, "
            + "taskId=task-123, documentId=document-123, reference=notification-123, "
            + "amount=250.00, totalClaimAmount=500.00, claimFeeInPence=7000, "
            + "interestAmount=15.00, paymentReference=RC-123";

        assertThat(PiiRedactor.redact(message))
            .contains(
                "caseId=1234567890123456",
                "caseReference=ABC-123",
                "userId=user-123",
                "taskId=task-123",
                "documentId=document-123",
                "reference=notification-123"
            )
            .doesNotContain("250.00", "500.00", "7000", "15.00", "RC-123")
            .contains(
                "amount=[REDACTED]",
                "totalClaimAmount=[REDACTED]",
                "claimFeeInPence=[REDACTED]",
                "interestAmount=[REDACTED]",
                "paymentReference=[REDACTED]"
            );
    }

    @Test
    void shouldRedactSensitiveValuesAcrossMultipleLines() {
        String message = "Request failed for jane.doe@example.com\nclaimAmount=250.00\nat Example.method";

        assertThat(PiiRedactor.redact(message))
            .isEqualTo("Request failed for [REDACTED]\nclaimAmount=[REDACTED]\nat Example.method");
    }

    @Test
    void shouldHandleNull() {
        assertThat(PiiRedactor.redact(null)).isNull();
    }
}
