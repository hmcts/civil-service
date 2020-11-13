package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowStateAllowedEventService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.validation.RequestExtensionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.REQUEST_EXTENSION;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.unspec.handler.callback.RequestExtensionCallbackHandler.ALREADY_AGREED;
import static uk.gov.hmcts.reform.unspec.handler.callback.RequestExtensionCallbackHandler.NOT_AGREED;
import static uk.gov.hmcts.reform.unspec.handler.callback.RequestExtensionCallbackHandler.PROPOSED_DEADLINE;
import static uk.gov.hmcts.reform.unspec.handler.callback.RequestExtensionCallbackHandler.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@SpringBootTest(classes = {
    RequestExtensionCallbackHandler.class,
    RequestExtensionValidator.class,
    JacksonAutoConfiguration.class,
    FlowStateAllowedEventService.class,
    StateFlowEngine.class,
    CaseDetailsConverter.class,
})
class RequestExtensionCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RequestExtensionCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateRespondedToClaim().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventProposeDeadlineCallback {

        private static final String PAGE_ID = "propose-deadline";

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalid() {
            CallbackParams params = callbackParamsOf(
                of(PROPOSED_DEADLINE, now().minusDays(1),
                   RESPONSE_DEADLINE, now().atTime(16, 0)
                ),
                MID,
                PAGE_ID
            );

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsOnly("The proposed deadline must be a date in the future");
        }

        @Test
        void shouldReturnNoError_whenValuesAreValid() {
            CallbackParams params = callbackParamsOf(
                of(PROPOSED_DEADLINE, now().plusDays(14),
                   RESPONSE_DEADLINE, now().atTime(16, 0)
                ),
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
        void shouldUpdateResponseDeadlineToProposedDeadline_whenExtensionAlreadyAgreed() {
            LocalDate proposedDeadline = now().plusDays(14);
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1claimResponseExtensionProposedDeadline(proposedDeadline)
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .respondentSolicitor1claimResponseExtensionAlreadyAgreed(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry(RESPONSE_DEADLINE, proposedDeadline.atTime(MID_NIGHT).toString());
        }

        @Test
        void shouldNotUpdateResponseDeadline_whenExtensionIsNotAlreadyAgreed() {
            LocalDate proposedDeadline = now().plusDays(14);
            LocalDateTime responseDeadline = now().atTime(16, 0, 0);
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1claimResponseExtensionProposedDeadline(proposedDeadline)
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .respondentSolicitor1claimResponseExtensionAlreadyAgreed(NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry(RESPONSE_DEADLINE, responseDeadline.format(ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateExtensionRequested().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsExactly(REQUEST_EXTENSION.name(), "READY");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenAlreadyAgreed() {
            LocalDate proposedDeadline = now().plusDays(14);
            LocalDateTime responseDeadline = now().atTime(16, 0);
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1claimResponseExtensionProposedDeadline(proposedDeadline)
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .respondentSolicitor1claimResponseExtensionCounterDate(responseDeadline.plusDays(7).toLocalDate())
                .respondentSolicitor1claimResponseExtensionAlreadyAgreed(YES)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# You asked for extra time to respond%n## Claim number: %s",
                        LEGACY_CASE_REFERENCE
                    ))
                    .confirmationBody(prepareBody(proposedDeadline, responseDeadline, ALREADY_AGREED))
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_whenNotAlreadyAgreed() {
            LocalDate proposedDeadline = now().plusDays(14);
            LocalDateTime responseDeadline = now().atTime(16, 0);
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1claimResponseExtensionProposedDeadline(proposedDeadline)
                .respondentSolicitor1ResponseDeadline(responseDeadline)
                .respondentSolicitor1claimResponseExtensionCounterDate(responseDeadline.plusDays(7).toLocalDate())
                .respondentSolicitor1claimResponseExtensionAlreadyAgreed(NO)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(
                        "# You asked for extra time to respond%n## Claim number: %s",
                        LEGACY_CASE_REFERENCE
                    ))
                    .confirmationBody(prepareBody(proposedDeadline, responseDeadline, NOT_AGREED))
                    .build());
        }

        private String prepareBody(LocalDate proposedDeadline, LocalDateTime responseDeadline, String notAgreed) {
            return format(
                "<br /><p>You asked if you can respond before 4pm on %s %s"
                    + "<p>They can choose not to respond to your request, so if you don't get an email from us, "
                    + "assume you need to respond before 4pm on %s.</p>",
                formatLocalDate(proposedDeadline, DATE),
                notAgreed,
                formatLocalDateTime(responseDeadline, DATE)
            );
        }
    }
}
