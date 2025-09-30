package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    MoveToAdditionalResponseTimeExpiredCallbackHandler.class,
    ParentCaseUpdateHelper.class
})
public class MoveToAdditionalResponseTimeExpiredCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private ParentCaseUpdateHelper parentCaseUpdateHelper;

    @Autowired
    private MoveToAdditionalResponseTimeExpiredCallbackHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRespondWithStateChanged() {
            CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1234L).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            assertThat(response.getState()).isEqualTo(ADDITIONAL_RESPONSE_TIME_EXPIRED.toString());
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldDispatchBusinessProcess_whenStatusIsReady() {
            CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1234L).build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            handler.handle(params);

            verify(parentCaseUpdateHelper, times(1)).updateParentWithGAState(
                caseData,
                ADDITIONAL_RESPONSE_TIME_EXPIRED.getDisplayedValue()
            );
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);
    }
}
