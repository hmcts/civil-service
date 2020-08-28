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
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.service.WorkingDayIndicator;
import uk.gov.hmcts.reform.unspec.service.docmosis.cos.CertificateOfServiceGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.handler.callback.ConfirmServiceCallbackHandler.CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ConfirmServiceCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
    DeadlinesCalculator.class
})
class ConfirmServiceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private CertificateOfServiceGenerator certificateOfServiceGenerator;
    @MockBean
    WorkingDayIndicator workingDayIndicator;

    @Autowired
    private ConfirmServiceCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPrepopulateServedDocumentsList_whenInvoked() {
            CallbackParams params = callbackParamsOf(new HashMap<>(), CallbackType.ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData())
                .containsOnly(Map.entry("servedDocuments", List.of(ServedDocuments.CLAIM_FORM)));
        }
    }

    @Nested
    class MidEventCallback {

        @Test
        void shouldReturnError_whenWhitespaceInServedDocumentsOther() {
            Map<String, Object> data = new HashMap<>();
            data.put("servedDocumentsOther", " ");

            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly(
                "CONTENT TBC: please enter a valid value for other documents");
        }

        @Test
        void shouldReturnNoError_whenValidServedDocumentsOther() {
            Map<String, Object> data = new HashMap<>();
            data.put("servedDocumentsOther", "A valid document");

            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).isEqualTo(data);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class SecondMidEventCallback {

        private final LocalDate claimIssueDate = LocalDate.of(2000, 6, 22);

        @Nested
        class ServiceDate {

            private final LocalDate today = LocalDate.now();
            private final LocalDate futureDate = today.plusYears(1);

            @Test
            void shouldReturnNoErrors_whenServiceDateInPastAndAfterIssueDate() {
                Map<String, Object> data = new HashMap<>();
                data.put("serviceMethod", Map.of("type", "POST"));
                data.put("serviceDate", claimIssueDate.plusDays(1));
                data.put("claimIssuedDate", claimIssueDate);

                CallbackParams params = callbackParamsOf(data, CallbackType.MID_SECONDARY);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnNoErrors_whenServiceDateIsTodayAndAfterIssueDate() {
                Map<String, Object> data = new HashMap<>();
                data.put("serviceMethod", Map.of("type", "POST"));
                data.put("serviceDate", today);
                data.put("claimIssuedDate", claimIssueDate);

                CallbackParams params = callbackParamsOf(data, CallbackType.MID_SECONDARY);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnError_whenServiceDateInFuture() {
                Map<String, Object> data = new HashMap<>();
                data.put("serviceMethod", Map.of("type", "POST"));
                data.put("serviceDate", futureDate);
                data.put("claimIssuedDate", claimIssueDate);

                CallbackParams params = callbackParamsOf(data, CallbackType.MID_SECONDARY);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).containsOnly("The date must not be in the future");
            }

            @Test
            void shouldReturnError_whenServiceDateIsBeforeClaimIssueDate() {
                Map<String, Object> data = new HashMap<>();
                data.put("serviceMethod", Map.of("type", "POST"));
                data.put("serviceDate", claimIssueDate.minusDays(1));
                data.put("claimIssuedDate", claimIssueDate);

                CallbackParams params = callbackParamsOf(data, CallbackType.MID_SECONDARY);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).containsOnly("The date must not be before issue date of claim");
            }
        }

        @Nested
        class ServiceDateAndTime {

            private final LocalDateTime today = LocalDateTime.now();
            private final LocalDateTime futureDate = today.plusYears(1);

            @Test
            void shouldReturnNoErrors_whenServiceDateInPastAndAfterIssueDate() {
                Map<String, Object> data = new HashMap<>();
                data.put("serviceMethod", Map.of("type", "FAX"));
                data.put("serviceDateAndTime", claimIssueDate.plusDays(1).atTime(12, 0));
                data.put("claimIssuedDate", claimIssueDate);

                CallbackParams params = callbackParamsOf(data, CallbackType.MID_SECONDARY);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnNoErrors_whenServiceDateIsTodayAndAfterIssueDate() {
                Map<String, Object> data = new HashMap<>();
                data.put("serviceMethod", Map.of("type", "FAX"));
                data.put("serviceDateAndTime", today);
                data.put("claimIssuedDate", claimIssueDate);

                CallbackParams params = callbackParamsOf(data, CallbackType.MID_SECONDARY);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldReturnError_whenServiceDateInFuture() {
                Map<String, Object> data = new HashMap<>();
                data.put("serviceMethod", Map.of("type", "FAX"));
                data.put("serviceDateAndTime", futureDate);
                data.put("claimIssuedDate", claimIssueDate);

                CallbackParams params = callbackParamsOf(data, CallbackType.MID_SECONDARY);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).containsOnly("The date must not be in the future");
            }

            @Test
            void shouldReturnError_whenServiceDateIsBeforeClaimIssueDate() {
                Map<String, Object> data = new HashMap<>();
                data.put("serviceMethod", Map.of("type", "FAX"));
                data.put("serviceDateAndTime", claimIssueDate.atTime(12, 0).minusDays(1));
                data.put("claimIssuedDate", claimIssueDate);

                CallbackParams params = callbackParamsOf(data, CallbackType.MID_SECONDARY);

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(response.getErrors()).containsOnly("The date must not be before issue date of claim");
            }
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnExpectedResponse_whenDateEntry() {
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            Map<String, Object> data = new HashMap<>();
            data.put("serviceMethod", Map.of("type", "POST"));
            data.put("serviceDate", "2099-06-23");

            CallbackParams params = callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                    "deemedDateOfService", LocalDate.of(2099, 6, 25),
                    "responseDeadline", LocalDateTime.of(2099, 7, 9, 23, 59, 59),
                    "serviceMethod", Map.of("type", "POST"),
                    "serviceDate", "2099-06-23",
                    "systemGeneratedCaseDocuments", emptyList()
                ));
        }

        @Test
        void shouldReturnExpectedResponse_whenDateAndTimeEntry() {
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);

            Map<String, Object> data = new HashMap<>();
            data.put("serviceMethod", Map.of("type", "FAX"));
            data.put("serviceDateAndTime", "2099-06-23T15:00:00");

            CallbackParams params = callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                    "deemedDateOfService", LocalDate.of(2099, 6, 23),
                    "responseDeadline", LocalDateTime.of(2099, 7, 7, 23, 59, 59),
                    "serviceMethod", Map.of("type", "FAX"),
                    "serviceDateAndTime", "2099-06-23T15:00:00",
                    "systemGeneratedCaseDocuments", emptyList()
                ));
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenValidData() {
            Map<String, Object> data = new HashMap<>();
            int documentSize = 0;
            LocalDate deemedDateOfService = LocalDate.now();
            LocalDateTime responseDeadline = deemedDateOfService.plusDays(14).atTime(16, 0);
            data.put("deemedDateOfService", deemedDateOfService);
            data.put("responseDeadline", responseDeadline);

            CallbackParams params = callbackParamsOf(data, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            String formattedDeemedDateOfService = formatLocalDate(deemedDateOfService, DATE);
            String responseDeadlineDate = formatLocalDateTime(responseDeadline, DATE_TIME_AT);

            String body = format(
                CONFIRMATION_SUMMARY,
                formattedDeemedDateOfService,
                responseDeadlineDate,
                format("/cases/case-details/%s#CaseDocuments", CASE_ID),
                documentSize / 1024
            );

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You've confirmed service")
                    .confirmationBody(body)
                    .build());
        }
    }
}
