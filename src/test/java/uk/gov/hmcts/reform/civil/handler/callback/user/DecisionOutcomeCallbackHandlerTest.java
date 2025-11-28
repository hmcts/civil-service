package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_DASHBOARD_TASK_LIST_DEFENDANT_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@ExtendWith(MockitoExtension.class)
class DecisionOutcomeCallbackHandlerTest extends BaseCallbackHandlerTest {

    private DecisionOutcomeCallbackHandler handler;

    private ObjectMapper objectMapper;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new DecisionOutcomeCallbackHandler(objectMapper, featureToggleService);
    }

    @Test
    void shouldReturnNoError_WhenAboutToSubmitIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateDecisionOutcome().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

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

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        CaseData updatedData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(response.getErrors()).isNull();
        assertThat(updatedData.getBusinessProcess()).isNotNull();
        assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(MOVE_TO_DECISION_OUTCOME.name());
        assertThat(updatedData.getBusinessProcess().getStatus()).isEqualTo(READY);
    }
}
