package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_UPDATE_GA_LOCATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_LOCATION_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenAppStateHelperService.class,
    TriggerGenAppLocationUpdateCallbackHandler.class,
    JacksonAutoConfiguration.class
})

 class TriggerGenAppLocationUpdateCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    TriggerGenAppLocationUpdateCallbackHandler handler;

    @MockBean
    private GenAppStateHelperService helperService;

    private static final String authToken = "Bearer TestAuthToken";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(TRIGGER_UPDATE_GA_LOCATION);
        assertThat(handler.handledEvents()).contains(TRIGGER_TASK_RECONFIG_GA);
    }

    @Test
    void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplication() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication()
            );
        when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
        CallbackParams params = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder()
                         .eventId(TRIGGER_UPDATE_GA_LOCATION.name())
                         .build())
            .build();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(), any());
        verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
        verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
        verifyNoMoreInteractions(helperService);
    }

    @Test
    void shouldNotTriggerGeneralApplicationEvent_whenCaseHasNoGeneralApplication() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verifyNoInteractions(helperService);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void triggerGeneralApplicationEventThrowsException_HandleFailure() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().ccdCaseReference(1234L).build(),
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication()
            );
        String expectedErrorMessage = "Could not trigger event to update location on application under case: "
            + caseData.getCcdCaseReference();
        when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
        when(helperService.triggerEvent(any(CaseData.class), eq(TRIGGER_LOCATION_UPDATE)))
            .thenThrow(new RuntimeException());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).contains(expectedErrorMessage);
    }

    @Test
    void shouldTriggerReconfigureWhenCallbackEventIsReconfigGA() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication()
            );
        when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .request(CallbackRequest.builder()
                         .eventId(TRIGGER_TASK_RECONFIG_GA.name())
                         .build())
            .build();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
        assertThat(response.getErrors()).isNull();
        verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_TASK_RECONFIG);
    }

    private Map<String, String> getOriginalStatusOfGeneralApplication() {
        Map<String, String> latestStatus = new HashMap<>();
        latestStatus.put("1234", "Application Submitted - Awaiting Judicial Decision");
        latestStatus.put("2345", "Order Made");
        return latestStatus;
    }

}
