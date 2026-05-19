package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DefaultJudgementGrantedSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @InjectMocks
    private DefaultJudgementGrantedSpecCallbackHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSetJudgmentStateAndBusinessProcess_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .ccdState(CaseState.JUDGMENT_REQUESTED)
                .activeJudgment(new JudgmentDetails())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getActiveJudgment().getState()).isEqualTo(JudgmentState.ISSUED);
            assertThat(updatedData.getActiveJudgment().getIssueDate()).isEqualTo(LocalDate.now());
            assertThat(updatedData.getActiveJudgment().getRtlState()).isEqualTo(JudgmentRTLStatus.ISSUED.getRtlState());
            assertThat(updatedData.getActiveJudgment().getIsRegisterWithRTL()).isEqualTo(YES);
            assertThat(response.getState()).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
        }

        @Test
        void shouldReturnError_whenCaseStateIsNotJudgmentRequested() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .ccdState(CaseState.CASE_ISSUED)
                .activeJudgment(new JudgmentDetails())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(format(
                "Event DEFAULT_JUDGEMENT_GRANTED_SPEC: Cannot grant default judgment for case in state %s for caseId %d",
                caseData.getCcdState(),
                caseData.getCcdCaseReference()
            ));
        }

        @Test
        void shouldReturnError_whenActiveJudgmentIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build()
                .toBuilder()
                .ccdState(CaseState.JUDGMENT_REQUESTED)
                .activeJudgment(null)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsExactly(format(
                "Event DEFAULT_JUDGEMENT_GRANTED_SPEC: Active judgment is null for caseId %d",
                caseData.getCcdCaseReference()
            ));
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenSubmitted() {
            String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Default Judgment Granted ")
                    .confirmationBody(format(body))
                    .build());
        }
    }
}
