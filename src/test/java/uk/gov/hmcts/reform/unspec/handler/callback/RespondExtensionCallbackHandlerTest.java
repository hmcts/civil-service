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
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowStateAllowedEventService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.validation.RequestExtensionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.reform.unspec.handler.callback.RespondExtensionCallbackHandler.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@SpringBootTest(classes = {
    RespondExtensionCallbackHandler.class,
    RequestExtensionValidator.class,
    JacksonAutoConfiguration.class,
    FlowStateAllowedEventService.class,
    StateFlowEngine.class,
    CaseDetailsConverter.class
})
class RespondExtensionCallbackHandlerTest extends BaseCallbackHandlerTest {

    public static final String RESPONSE_DEADLINE = "respondentSolicitor1ResponseDeadline";
    public static final String COUNTER_DATE = "respondentSolicitor1claimResponseExtensionCounterDate";
    public static final String COUNTER = "respondentSolicitor1claimResponseExtensionCounter";
    public static final String REFERENCE_NUMBER = "000LR001";

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
    class MidEventCallback {

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalid() {
            CallbackParams params = callbackParamsOf(
                of(COUNTER_DATE, now().minusDays(1),
                   COUNTER, YesOrNo.YES,
                   RESPONSE_DEADLINE, now().atTime(MID_NIGHT)
                ),
                CallbackType.MID
            );

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors())
                .containsOnly("The proposed deadline must be a date in the future");
        }

        @Test
        void shouldReturnNoError_whenValuesAreValid() {
            CallbackParams params = callbackParamsOf(
                of(COUNTER_DATE, now().plusDays(14),
                   COUNTER, YesOrNo.YES,
                   RESPONSE_DEADLINE, now().atTime(MID_NIGHT)
                ),
                CallbackType.MID
            );

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenCounterDateIsNo() {
            CallbackParams params = callbackParamsOf(of(COUNTER, YesOrNo.NO), CallbackType.MID);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        public static final String PROPOSED_DEADLINE = "respondentSolicitor1claimResponseExtensionProposedDeadline";
        public static final String ACCEPT = "respondentSolicitor1claimResponseExtensionAccepted";

        @Test
        void shouldUpdateResponseDeadlineToProposedDeadline_whenAcceptIsYes() {
            LocalDate proposedDeadline = now().plusDays(14);
            Map<String, Object> map = new HashMap<>();
            map.put(PROPOSED_DEADLINE, proposedDeadline);
            map.put(RESPONSE_DEADLINE, now().atTime(MID_NIGHT));
            map.put(ACCEPT, YesOrNo.YES);

            CallbackParams params = callbackParamsOf(map, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsEntry(RESPONSE_DEADLINE, proposedDeadline.atTime(MID_NIGHT));
        }

        @Test
        void shouldUpdateResponseDeadlineToCounterDate_whenAcceptIsNoAndCounterIsYes() {
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);

            Map<String, Object> map = new HashMap<>();
            map.put(RESPONSE_DEADLINE, responseDeadline);
            map.put(PROPOSED_DEADLINE, responseDeadline.plusDays(14).toLocalDate());
            map.put(COUNTER_DATE, responseDeadline.plusDays(7).toLocalDate());
            map.put(ACCEPT, YesOrNo.NO);
            map.put(COUNTER, YesOrNo.YES);

            CallbackParams params = callbackParamsOf(map, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsEntry(RESPONSE_DEADLINE, responseDeadline.plusDays(7));
        }

        @Test
        void shouldKeepExistingResponseDeadline_whenAcceptIsNoAndCounterIsNo() {
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);

            Map<String, Object> map = new HashMap<>();
            map.put(RESPONSE_DEADLINE, responseDeadline);
            map.put(COUNTER, YesOrNo.NO);
            map.put(ACCEPT, YesOrNo.NO);

            CallbackParams params = callbackParamsOf(map, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).containsEntry(RESPONSE_DEADLINE, responseDeadline);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_withNewResponseDeadline() {
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);
            CallbackParams params = callbackParamsOf(
                of(RESPONSE_DEADLINE, responseDeadline,
                   LEGACY_CASE_REFERENCE, REFERENCE_NUMBER
                ), CallbackType.SUBMITTED
            );

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String expectedBody = format(
                "<br />The defendant must respond before 4pm on %s", formatLocalDateTime(responseDeadline, DATE));

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# You've responded to the request for more time%n## Claim number: %s",
                        REFERENCE_NUMBER
                    ))
                    .confirmationBody(expectedBody)
                    .build());
        }
    }
}
