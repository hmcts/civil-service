package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.BusinessProcessService;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.WorkingDayIndicator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.CLAIM_FORM;
import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.FAX;
import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.POST;
import static uk.gov.hmcts.reform.unspec.handler.callback.ConfirmServiceCallbackHandler.CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.DEEMED_SERVICE_DATE;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ConfirmServiceCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    DeadlinesCalculator.class,
    CaseDetailsConverter.class,
    BusinessProcessService.class
})
class ConfirmServiceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    WorkingDayIndicator workingDayIndicator;

    @Autowired
    private ConfirmServiceCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPrepopulateServedDocumentsList_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_START);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("servedDocuments").isEqualTo(List.of(CLAIM_FORM.name()));
        }
    }

    @Nested
    class MidEventServedDocumentCallback {

        private static final String PAGE_ID = "served-documents";

        @Test
        void shouldReturnError_whenWhitespaceInServedDocumentsOther() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated()
                .servedDocumentsOther(" ")
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(
                "CONTENT TBC: please enter a valid value for other documents");
        }

        @Test
        void shouldReturnNoError_whenValidServedDocumentsOther() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventServiceDateCallback {

        private final LocalDate claimIssueDate = LocalDate.of(2000, 6, 22);

        @Nested
        class ServiceDate {

            private static final String PAGE_ID = "service-date";
            private final LocalDate today = LocalDate.now();
            private final LocalDate futureDate = today.plusYears(1);

            @Test
            void shouldReturnNoErrors_whenServiceDateInPastAndAfterIssueDate() {
                CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                    .claimIssuedDate(claimIssueDate)
                    .serviceDateToRespondentSolicitor1(claimIssueDate.plusDays(1))
                    .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(POST).build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnNoErrors_whenServiceDateIsTodayAndAfterIssueDate() {
                CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                    .claimIssuedDate(claimIssueDate)
                    .serviceDateToRespondentSolicitor1(today)
                    .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(POST).build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnError_whenServiceDateInFuture() {
                CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                    .claimIssuedDate(claimIssueDate)
                    .serviceDateToRespondentSolicitor1(futureDate)
                    .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(POST).build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).containsOnly("The date must not be in the future");
            }

            @Test
            void shouldReturnError_whenServiceDateIsBeforeClaimIssueDate() {
                CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                    .claimIssuedDate(claimIssueDate)
                    .serviceDateToRespondentSolicitor1(claimIssueDate.minusDays(3))
                    .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(POST).build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).containsOnly("The date must not be before issue date of claim");
            }
        }

        @Nested
        class ServiceDateAndTime {

            private final LocalDateTime today = LocalDateTime.now();
            private final LocalDateTime futureDate = today.plusYears(1);

            @Test
            void shouldReturnNoErrors_whenServiceDateInPastAndAfterIssueDate() {
                CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                    .claimIssuedDate(claimIssueDate)
                    .serviceDateTimeToRespondentSolicitor1(claimIssueDate.plusDays(1).atTime(12, 0))
                    .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(FAX).build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, "service-date");

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnNoErrors_whenServiceDateIsTodayAndAfterIssueDate() {
                CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                    .claimIssuedDate(claimIssueDate)
                    .serviceDateTimeToRespondentSolicitor1(today)
                    .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(FAX).build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, "service-date");

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnError_whenServiceDateInFuture() {
                CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                    .claimIssuedDate(claimIssueDate)
                    .serviceDateTimeToRespondentSolicitor1(futureDate)
                    .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(FAX).build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, "service-date");

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).containsOnly("The date must not be in the future");
            }

            @Test
            void shouldReturnError_whenServiceDateIsBeforeClaimIssueDate() {
                CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                    .claimIssuedDate(claimIssueDate)
                    .serviceDateTimeToRespondentSolicitor1(claimIssueDate.atTime(12, 0).minusDays(1))
                    .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(FAX).build())
                    .build();
                CallbackParams params = callbackParamsOf(caseData, MID, "service-date");

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getErrors()).containsOnly("The date must not be before issue date of claim");
            }
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnExpectedResponseAndMarkBusinessProcessReady_whenDateEntry() {
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                .serviceDateToRespondentSolicitor1(LocalDate.of(2099, 6, 23))
                .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(POST).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsAllEntriesOf(
                Map.of(
                    "deemedServiceDateToRespondentSolicitor1", LocalDate.of(2099, 6, 25).toString(),
                    "respondentSolicitor1ResponseDeadline", LocalDateTime.of(2099, 7, 9, 23, 59, 59).toString(),
                    "businessProcess", Map.of("status", "READY", "camundaEvent", "CONFIRM_SERVICE")
                ));
        }

        @Test
        void shouldReturnExpectedResponseAndMarkBusinessProcessReady_whenDateAndTimeEntry() {
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed()
                .serviceDateTimeToRespondentSolicitor1(LocalDateTime.of(2099, 6, 23, 15, 0, 0))
                .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(FAX).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsAllEntriesOf(
                Map.of(
                    "deemedServiceDateToRespondentSolicitor1", LocalDate.of(2099, 6, 23).toString(),
                    "respondentSolicitor1ResponseDeadline", LocalDateTime.of(2099, 7, 7, 23, 59, 59).toString(),
                    "businessProcess", Map.of("status", "READY", "camundaEvent", "CONFIRM_SERVICE")
                ));
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenValidData() {
            CaseData caseData = CaseDataBuilder.builder().atStateServiceConfirmed().build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            String formattedDeemedDateOfService = formatLocalDate(DEEMED_SERVICE_DATE, DATE);
            String responseDeadlineDate = formatLocalDateTime(RESPONSE_DEADLINE, DATE_TIME_AT);

            String body = format(
                CONFIRMATION_SUMMARY,
                formattedDeemedDateOfService,
                responseDeadlineDate,
                format("/cases/case-details/%s#CaseDocuments", CASE_ID)
            );

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You've confirmed service")
                    .confirmationBody(body)
                    .build());
        }
    }
}
