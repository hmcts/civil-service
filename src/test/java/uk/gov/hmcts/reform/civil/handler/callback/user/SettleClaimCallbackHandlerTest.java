package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;

@ExtendWith(MockitoExtension.class)
class SettleClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private SettleClaimCallbackHandler handler;

    @Mock
    private TaskListService taskListService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DashboardNotificationService dashboardNotificationService;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void should_go_to_claim_settled_state() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState()).isNotNull();
            assertThat(response.getState()).isEqualTo(CASE_SETTLED.name());
        }

        @Test
        void should_go_to_claim_settled_stateForLipvLr() {
            CaseData caseData = CaseDataBuilder.builder().specClaim1v1LipvLr().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState()).isNotNull();
            assertThat(response.getState()).isEqualTo(CASE_SETTLED.name());
        }

        @Test
        void should_go_to_claim_settled_stateForLrvLip() {
            CaseData caseData = CaseDataBuilder.builder().specClaim1v1LrVsLip().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getState()).isNotNull();
            assertThat(response.getState()).isEqualTo(CASE_SETTLED.name());
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void should_include_header_and_body() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);
            Assertions.assertTrue(response.getConfirmationHeader().contains("# Claim marked as settled"));
            Assertions.assertTrue(response.getConfirmationBody().contains("<br />"));
        }

        @Test
        void should_disable_task_list_items() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            caseData = caseData.toBuilder()
                .ccdCaseReference(1234L)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(SUBMITTED, caseData).build();
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
                .handle(params);
            verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
                caseData.getCcdCaseReference().toString(),
                "CLAIMANT",
                "Application.View"
            );
            verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
                caseData.getCcdCaseReference().toString(),
                "DEFENDANT",
                "Application.View"
            );
            Assertions.assertTrue(response.getConfirmationHeader().contains("# Claim marked as settled"));
            Assertions.assertTrue(response.getConfirmationBody().contains("<br />"));
        }
    }
}
