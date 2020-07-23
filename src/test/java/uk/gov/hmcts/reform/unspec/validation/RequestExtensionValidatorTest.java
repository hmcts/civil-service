package uk.gov.hmcts.reform.unspec.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.handler.RequestExtensionCallbackHandler.PROPOSED_DEADLINE;
import static uk.gov.hmcts.reform.unspec.handler.RequestExtensionCallbackHandler.RESPONSE_DEADLINE;

@SpringBootTest(classes = {RequestExtensionValidator.class, JacksonAutoConfiguration.class})
class RequestExtensionValidatorTest {

    @Autowired
    RequestExtensionValidator validator;

    @Nested
    class ValidateProposedDeadLine {

        @Test
        void shouldReturnNoErrors_whenValidProposedDeadline() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(PROPOSED_DEADLINE, now().plusDays(14),
                         RESPONSE_DEADLINE, now().plusDays(7).atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors.isEmpty()).isTrue();
        }

        @Test
        void shouldReturnError_whenProposedDeadlineIsNotProvided() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(RESPONSE_DEADLINE, now().atTime(16, 0)))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors).containsOnly("The proposed deadline must be provided");
        }

        @Test
        void shouldReturnErrors_whenProposedDeadlineIsAfter28DaysFromResponseDeadline() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(PROPOSED_DEADLINE, now().plusDays(29),
                         RESPONSE_DEADLINE, now().atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors)
                .containsOnly("The proposed deadline cannot be more than 28 days after the current deadline");
        }

        @Test
        void shouldReturnError_whenProposedDeadlineInIsNotInFuture() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(PROPOSED_DEADLINE, now(),
                         RESPONSE_DEADLINE, now().atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors)
                .containsOnly("The proposed deadline must be a date in the future");
        }

        @Test
        void shouldReturnError_whenProposedDeadlineInIsSameAsResponseDeadline() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(PROPOSED_DEADLINE, now().plusDays(5),
                         RESPONSE_DEADLINE, now().plusDays(5).atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors)
                .containsOnly("The proposed deadline must be after the current deadline");
        }

        @Test
        void shouldReturnError_whenProposedDeadlineInIsBeforeResponseDeadline() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(PROPOSED_DEADLINE, now().plusDays(4),
                         RESPONSE_DEADLINE, now().plusDays(5).atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateProposedDeadline(caseDetails);

            assertThat(errors)
                .containsOnly("The proposed deadline must be after the current deadline");
        }
    }

    @Nested
    class ExtensionAlreadyRequested {

        @Test
        void shouldReturnErrors_whenExtensionAlreadyRequested() {
            CaseDetails caseDetails = CaseDetails.builder()
                .data(of(PROPOSED_DEADLINE, now().plusDays(14),
                         RESPONSE_DEADLINE, now().plusDays(7).atTime(16, 0)
                ))
                .build();

            List<String> errors = validator.validateAlreadyRequested(caseDetails);

            assertThat(errors)
                .containsOnly("You can only request an extension once");
        }

        @Test
        void shouldReturnNoError_whenExtensionRequestedFirstTime() {
            List<String> errors = validator.validateAlreadyRequested(
                CaseDetails.builder()
                    .data(of(RESPONSE_DEADLINE, now().atTime(16, 0)))
                    .build()
            );

            assertThat(errors).isEmpty();
        }
    }
}
