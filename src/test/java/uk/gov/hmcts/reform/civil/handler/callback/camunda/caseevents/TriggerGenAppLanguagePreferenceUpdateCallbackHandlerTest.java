package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;


import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_GA_LANGUAGE_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_LANGUAGE_PREFERENCE;

@ExtendWith(MockitoExtension.class)
public class TriggerGenAppLanguagePreferenceUpdateCallbackHandlerTest {

    @InjectMocks
    TriggerGenAppLanguagePreferenceUpdateCallbackHandler handler;

    @Mock
    private GenAppStateHelperService helperService;


    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplication() {
            CaseData caseData = CaseData.builder().build();

            when(helperService.triggerEvent(any(), any())).thenReturn(true);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(UPDATE_GA_LANGUAGE_PREFERENCE.name())
                             .build())
                .build();
            handler.handle(params);

            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_GA_LANGUAGE_UPDATE);
            verifyNoMoreInteractions(helperService);
        }

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).containsExactly(UPDATE_GA_LANGUAGE_PREFERENCE);
    }
}
