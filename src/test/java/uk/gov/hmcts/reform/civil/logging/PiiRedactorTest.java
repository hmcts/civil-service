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
    void shouldHandleNull() {
        assertThat(PiiRedactor.redact(null)).isNull();
    }
}
