package uk.gov.hmcts.reform.unspec.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.validation.DeadlineExtensionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@SpringBootTest(classes = {
    InformAgreedExtensionDateCallbackHandler.class,
    DeadlineExtensionValidator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    DeadlinesCalculator.class
})
class InformAgreedExtensionDateCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private InformAgreedExtensionDateCallbackHandler handler;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateClaimCreated().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class ExtensionValidation {

        private static final String PAGE_ID = "extension-date";
        private static final String EXTENSION_DATE = "respondentSolicitor1AgreedDeadlineExtension";
        private static final String RESPONSE_DEADLINE = "respondentSolicitor1ResponseDeadline";

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalid() {
            CallbackParams params = callbackParamsOf(
                of(EXTENSION_DATE, now().minusDays(1), RESPONSE_DEADLINE, now().atTime(16, 0)),
                MID,
                PAGE_ID
            );

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsOnly("The agreed extension date must be a date in the future");
        }

        @Test
        void shouldReturnNoError_whenValuesAreValid() {
            CallbackParams params = callbackParamsOf(
                of(EXTENSION_DATE, now().plusDays(14), RESPONSE_DEADLINE, now().atTime(16, 0)),
                MID,
                PAGE_ID
            );

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdateResponseDeadlineToExtensionDate_whenInvoked() {
            LocalDate extensionDate = now().plusDays(14);
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);

            given(deadlinesCalculator.calculateFirstWorkingDay(extensionDate)).willReturn(extensionDate);

            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1AgreedDeadlineExtension(extensionDate)
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry(
                    "respondentSolicitor1ResponseDeadline",
                    formatLocalDateTime(extensionDate.atTime(END_OF_BUSINESS_DAY), "yyyy-MM-dd'T'HH:mm:ss")
                );
        }
    }

    @Nested
    class SubmittedCallback {

        private static final String BODY = "<br />What happens next.%n%n You must respond to the claimant by %s";

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            LocalDateTime responseDeadline = now().atTime(END_OF_BUSINESS_DAY);
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Extension deadline submitted")
                    .confirmationBody(format(BODY, formatLocalDateTime(responseDeadline, DATE)))
                    .build());
        }
    }
}
