package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;

@SpringBootTest(classes = {
    SettleClaimCallbackHandler.class,
    ObjectMapper.class
})
class SettleClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private SettleClaimCallbackHandler handler;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private DashboardApiClient dashboardApiClient;

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
    }
}
