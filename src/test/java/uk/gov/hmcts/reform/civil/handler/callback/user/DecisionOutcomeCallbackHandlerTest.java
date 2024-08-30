package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME;

@ExtendWith(MockitoExtension.class)
class DecisionOutcomeCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DecisionOutcomeCallbackHandler handler;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void shouldReturnNoError_WhenAboutToSubmitIsInvoked() {
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateDecisionOutcome().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseDetails).build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNoError_WhenAboutToSubmitIsInvokedAndCasePRogressionEnabled() {
        CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME.name()).build()
        ).build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldHandleOnlyMoveToDecisionOutcomeEvent() {
        Map<String, CallbackHandler> handlers = new HashMap<>();

        handler.register(handlers);

        assertThat(handlers).containsExactly(entry(MOVE_TO_DECISION_OUTCOME.toString(), handler));
    }

}
