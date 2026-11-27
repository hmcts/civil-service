package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.nio.charset.StandardCharsets;
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
import static uk.gov.hmcts.reform.civil.callback.CallbackFailureClassifier.NOT_RETRYABLE;
import static uk.gov.hmcts.reform.civil.callback.CallbackFailureClassifier.RETRYABLE;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE;

@ExtendWith(MockitoExtension.class)
class TriggerMoveApplicationOfflineCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    TriggerMoveApplicationOfflineCallbackHandler handler;

    @Mock
    private GenAppStateHelperService helperService;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE);
    }

    @Test
    void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplication() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseDataBuilder.builder().build(),
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication()
            );
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        verify(helperService, times(1)).triggerEvent(caseData, APPLICATION_PROCEEDS_IN_HERITAGE);
        verify(helperService, times(1)).triggerEvent(caseData, APPLICATION_PROCEEDS_IN_HERITAGE);
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
        CaseData baseCaseData = CaseDataBuilder.builder().build();
        baseCaseData.setCcdCaseReference(1234L);
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(baseCaseData,
                                        true,
                                        true,
                                        true, true,
                                        getOriginalStatusOfGeneralApplication()
            );
        String expectedErrorMessage = "Could not trigger event to take application offline under the case: "
            + caseData.getCcdCaseReference();
        when(helperService.triggerEvent(any(CaseData.class), eq(APPLICATION_PROCEEDS_IN_HERITAGE)))
            .thenThrow(new RuntimeException());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).singleElement().asString()
            .startsWith(NOT_RETRYABLE + expectedErrorMessage + " (java.lang.RuntimeException");
    }

    @Test
    void transientCcdFailureIsLabelledRetryable() {
        // 2 Nov 2026: CCD's connection pool was exhausted mid-loop, a retry would have worked
        CaseData baseCaseData = CaseDataBuilder.builder().build();
        baseCaseData.setCcdCaseReference(1234L);
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(baseCaseData, true, true, true, true,
                                        getOriginalStatusOfGeneralApplication());
        Request request = Request.create(Request.HttpMethod.GET, "http://ccd-data-store-api/event-triggers/token",
                                         Map.of(), null, StandardCharsets.UTF_8, null);
        when(helperService.triggerEvent(any(CaseData.class), eq(APPLICATION_PROCEEDS_IN_HERITAGE)))
            .thenThrow(new FeignException.InternalServerError("[500 Internal Server Error]", request, null, null));

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        assertThat(response.getErrors()).singleElement().asString()
            .startsWith(RETRYABLE + "Could not trigger event to take application offline under the case: " + caseData.getCcdCaseReference());
    }

    private Map<String, String> getOriginalStatusOfGeneralApplication() {
        Map<String, String> latestStatus = new HashMap<>();
        latestStatus.put("1234", "Application Submitted - Awaiting Judicial Decision");
        latestStatus.put("2345", "Order Made");
        return latestStatus;
    }

}
