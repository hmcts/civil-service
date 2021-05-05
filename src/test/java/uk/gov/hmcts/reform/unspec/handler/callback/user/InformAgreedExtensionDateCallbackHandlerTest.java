package uk.gov.hmcts.reform.unspec.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.unspec.service.Time;
import uk.gov.hmcts.reform.unspec.validation.DeadlineExtensionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.LocalTime.MIDNIGHT;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

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

    @MockBean
    private Time time;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class ExtensionValidation {

        private static final String PAGE_ID = "extension-date";

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalid() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .extensionDate(now().minusDays(1))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsOnly("The agreed extension date must be a date in the future");
        }

        @Test
        void shouldReturnNoError_whenValuesAreValid() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .extensionDate(RESPONSE_DEADLINE.toLocalDate().plusDays(14))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {
        LocalDateTime timeExtensionDate;
        LocalDate extensionDate;

        @BeforeEach
        void setup() {
            timeExtensionDate = LocalDateTime.of(2020, 1, 1, 12, 0, 0);
            when(time.now()).thenReturn(timeExtensionDate);

            extensionDate = now().plusDays(14);
            when(deadlinesCalculator.calculateFirstWorkingDay(extensionDate)).thenReturn(extensionDate);
        }

        @Test
        void shouldUpdateResponseDeadlineToExtensionDate_whenInvoked() {
            LocalDateTime responseDeadline = now().atTime(MIDNIGHT);

            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1AgreedDeadlineExtension(extensionDate)
                .respondent1ResponseDeadline(responseDeadline)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            LocalDateTime newDeadline = extensionDate.atTime(END_OF_BUSINESS_DAY);

            assertThat(response.getData())
                .containsEntry("respondent1ResponseDeadline", newDeadline.format(ISO_DATE_TIME))
                .containsEntry("respondent1TimeExtensionDate", timeExtensionDate.format(ISO_DATE_TIME));
        }
    }

    @Nested
    class SubmittedCallback {

        private static final String BODY = "<br />What happens next.%n%n You must respond to the claimant by %s";

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            LocalDateTime responseDeadline = now().atTime(END_OF_BUSINESS_DAY);
            CaseData caseData = CaseDataBuilder.builder()
                .respondent1ResponseDeadline(responseDeadline)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Extension deadline submitted")
                    .confirmationBody(format(BODY, formatLocalDateTime(responseDeadline, DATE_TIME_AT)))
                    .build());
        }
    }
}
