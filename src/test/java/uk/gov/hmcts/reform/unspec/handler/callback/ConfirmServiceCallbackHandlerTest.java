package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.service.docmosis.cos.CertificateOfServiceGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.handler.callback.ConfirmServiceCallbackHandler.CONFIRMATION_SUMMARY;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    ConfirmServiceCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class ConfirmServiceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private CertificateOfServiceGenerator certificateOfServiceGenerator;

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
    class AboutToSubmitCallback {

        @Test
        void shouldReturnExpectedResponse_whenDateEntry() {
            Map<String, Object> data = new HashMap<>();
            data.put("serviceMethod", Map.of("type", "POST"));
            data.put("serviceDate", "2099-06-23");

            CallbackParams params = callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                    "deemedDateOfService", LocalDate.of(2099, 6, 25),
                    "responseDeadline", LocalDateTime.of(2099, 7, 9, 16, 0),
                    "serviceMethod", Map.of("type", "POST"),
                    "serviceDate", "2099-06-23",
                    "systemGeneratedCaseDocuments", emptyList()
                ));
        }

        @Test
        void shouldReturnExpectedResponse_whenDateAndTimeEntry() {
            Map<String, Object> data = new HashMap<>();
            data.put("serviceMethod", Map.of("type", "FAX"));
            data.put("serviceDateAndTime", "2099-06-23T15:00:00");

            CallbackParams params = callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).containsExactlyInAnyOrderEntriesOf(
                Map.of(
                    "deemedDateOfService", LocalDate.of(2099, 6, 23),
                    "responseDeadline", LocalDateTime.of(2099, 7, 7, 16, 0),
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
            data.put("deemedDateOfService", deemedDateOfService);

            CallbackParams params = callbackParamsOf(data, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            String formattedDeemedDateOfService = formatLocalDate(deemedDateOfService, DATE);
            String responseDeadlineDate = formatLocalDateTime(
                deemedDateOfService.plusDays(14).atTime(16, 0),
                DATE_TIME_AT
            );

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
