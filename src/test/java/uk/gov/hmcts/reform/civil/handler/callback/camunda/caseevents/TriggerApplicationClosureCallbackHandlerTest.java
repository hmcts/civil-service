package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_APPLICATION_CLOSURE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    TriggerApplicationClosureCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class TriggerApplicationClosureCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    TriggerApplicationClosureCallbackHandler handler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(TRIGGER_APPLICATION_CLOSURE);
    }

    @Test
    void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplication() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build(),
                        true,
                        true,
                        true,
                        getOriginalStatusOfGeneralApplication());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        verify(coreCaseDataService, times(1)).triggerGeneralApplicationEvent(1234L, MAIN_CASE_CLOSED);
        verify(coreCaseDataService, times(1)).triggerGeneralApplicationEvent(2345L, MAIN_CASE_CLOSED);
        verifyNoMoreInteractions(coreCaseDataService);
    }

    @Test
    void shouldNotTriggerGeneralApplicationEvent_whenCaseHasNoGeneralApplication() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verifyNoInteractions(coreCaseDataService);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void triggerGeneralApplicationEventThrowsException_HandleFailure() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().ccdCaseReference(1234L).build(),
                        true,
                        true,
                        true,
                        getOriginalStatusOfGeneralApplication());
        String expectedErrorMessage = "Could not trigger event to close application under case: "
                + caseData.getCcdCaseReference();
        when(coreCaseDataService.triggerGeneralApplicationEvent(anyLong(), eq(MAIN_CASE_CLOSED)))
                .thenThrow(new RuntimeException());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    private Map<String, String> getOriginalStatusOfGeneralApplication() {
        Map<String, String> latestStatus = new HashMap<>();
        latestStatus.put("1234", "Application Submitted - Awaiting Judicial Decision");
        latestStatus.put("2345", "Order Made");
        return latestStatus;
    }

}