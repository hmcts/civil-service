package uk.gov.hmcts.reform.unspec.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    DeadlineExtensionValidator.class,
    JacksonAutoConfiguration.class
})
class DeadlineExtensionValidatorTest {

    @Autowired
    DeadlineExtensionValidator validator;

    private static final String AGREED_DEADLINE_EXTENSION = "respondentSolicitor1AgreedDeadlineExtension";
    private static final String RESPONSE_DEADLINE = "respondentSolicitor1ResponseDeadline";

    @Nested
    class ValidateProposedDeadLine {

        @Test
        void shouldReturnNoErrors_whenExtension() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(AGREED_DEADLINE_EXTENSION, now().plusDays(14),
                         RESPONSE_DEADLINE, now().plusDays(7).atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors.isEmpty()).isTrue();
        }

        @Test
        void shouldReturnErrors_whenExtensionIsMoreThan28DaysFromResponseDeadline() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(AGREED_DEADLINE_EXTENSION, now().plusDays(29),
                         RESPONSE_DEADLINE, now().atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors)
                .containsOnly("The agreed extension date cannot be more than 28 days after the current deadline");
        }

        @Test
        void shouldReturnError_whenExtensionInPast() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(AGREED_DEADLINE_EXTENSION, now(),
                         RESPONSE_DEADLINE, now().atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors).containsOnly("The agreed extension date must be a date in the future");
        }

        @Test
        void shouldReturnError_whenExtensionIsSameAsResponseDeadline() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(AGREED_DEADLINE_EXTENSION, now().plusDays(5),
                         RESPONSE_DEADLINE, now().plusDays(5).atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors).containsOnly("The agreed extension date must be after the current deadline");
        }

        @Test
        void shouldReturnError_whenExtensionIsBeforeResponseDeadline() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(AGREED_DEADLINE_EXTENSION, now().plusDays(4),
                         RESPONSE_DEADLINE, now().plusDays(5).atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors).containsOnly("The agreed extension date must be after the current deadline");
        }

        @Test
        void shouldReturnNoErrors_whenIndividualDates() {
            LocalDate proposedDeadline = now().plusDays(14);
            LocalDateTime responseDeadline = now().plusDays(7).atTime(16, 0);

            List<String> errors = validator.validateProposedDeadline(proposedDeadline, responseDeadline);

            assertThat(errors).isEmpty();
        }
    }
}
