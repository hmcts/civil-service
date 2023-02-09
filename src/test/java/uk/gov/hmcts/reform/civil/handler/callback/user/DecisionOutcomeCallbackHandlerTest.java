package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;

@SpringBootTest(classes = {
    DecisionOutcomeCallbackHandler.class
})
class DecisionOutcomeCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DecisionOutcomeCallbackHandler handler;

    @Test
    void shouldReturnNoError_WhenAboutToSubmitIsInvoked() {
        CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateDecisionOutcome().build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseDetails).build();

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
