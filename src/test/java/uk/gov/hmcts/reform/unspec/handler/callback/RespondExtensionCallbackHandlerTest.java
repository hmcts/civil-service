package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowStateAllowedEventService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.validation.RequestExtensionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.RESPOND_EXTENSION;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@SpringBootTest(classes = {
    RespondExtensionCallbackHandler.class,
    RequestExtensionValidator.class,
    JacksonAutoConfiguration.class,
    FlowStateAllowedEventService.class,
    StateFlowEngine.class,
    CaseDetailsConverter.class,
})
class RespondExtensionCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String RESPONSE_DEADLINE = "respondentSolicitor1ResponseDeadline";

    @Autowired
    private RespondExtensionCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        public static final String EXTENSION_REASON = "respondentSolicitor1claimResponseExtensionReason";

        @Test
        void shouldAddNoReasonGiven_WhenNoReasonGivenForExtensionRequest() {
            CallbackParams params = callbackParamsOf(new HashMap<>(), CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsOnly(entry(EXTENSION_REASON, "No reason given"));
        }

        @Test
        void shouldKeepReasonGiven_WhenReasonGivenForExtensionRequest() {
            Map<String, Object> data = new HashMap<>();
            data.put(EXTENSION_REASON, "Reason given");
            CallbackParams params = callbackParamsOf(data, CallbackType.ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsOnly(entry(EXTENSION_REASON, "Reason given"));
        }
    }

    @Nested
    class MidEventCounterCallback {

        private static final String PAGE_ID = "counter";

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalid() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1claimResponseExtensionCounterDate(now().minusDays(1))
                .respondentSolicitor1ResponseDeadline(now().atTime(MID_NIGHT))
                .respondentSolicitor1claimResponseExtensionCounter(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors())
                .containsOnly("The proposed deadline must be a date in the future");
        }

        @Test
        void shouldReturnNoError_whenValuesAreValid() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1claimResponseExtensionCounterDate(now().plusDays(14))
                .respondentSolicitor1ResponseDeadline(now().atTime(MID_NIGHT))
                .respondentSolicitor1claimResponseExtensionCounter(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenCounterDateIsNo() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1claimResponseExtensionCounter(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldUpdateResponseDeadlineToProposedDeadline_whenAcceptIsYes() {
            LocalDate proposedDeadline = now().plusDays(14);
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1claimResponseExtensionProposedDeadline(proposedDeadline)
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .respondentSolicitor1claimResponseExtensionAccepted(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry(RESPONSE_DEADLINE, proposedDeadline.atTime(MID_NIGHT).toString());
        }

        @Test
        void shouldUpdateResponseDeadlineToCounterDate_whenAcceptIsNoAndCounterIsYes() {
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .respondentSolicitor1claimResponseExtensionProposedDeadline(responseDeadline.plusDays(14).toLocalDate())
                .respondentSolicitor1claimResponseExtensionCounterDate(responseDeadline.plusDays(7).toLocalDate())
                .respondentSolicitor1claimResponseExtensionCounter(YES)
                .respondentSolicitor1claimResponseExtensionAccepted(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry(RESPONSE_DEADLINE, responseDeadline.plusDays(7).toString());
        }

        @Test
        void shouldKeepExistingResponseDeadline_whenAcceptIsNoAndCounterIsNo() {
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .respondentSolicitor1claimResponseExtensionCounter(NO)
                .respondentSolicitor1claimResponseExtensionAccepted(NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsEntry(RESPONSE_DEADLINE, responseDeadline.toString());
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1ResponseDeadline(now().atTime(MID_NIGHT))
                .respondentSolicitor1claimResponseExtensionCounter(NO)
                .respondentSolicitor1claimResponseExtensionAccepted(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(RESPOND_EXTENSION.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_withNewResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionResponded().build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String expectedBody = format(
                "<br />The defendant must respond before 4pm on %s",
                formatLocalDateTime(CaseDataBuilder.RESPONSE_DEADLINE, DATE)
            );

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# You've responded to the request for more time%n## Claim number: %s",
                        LEGACY_CASE_REFERENCE
                    ))
                    .confirmationBody(expectedBody)
                    .build());
        }
    }
}
