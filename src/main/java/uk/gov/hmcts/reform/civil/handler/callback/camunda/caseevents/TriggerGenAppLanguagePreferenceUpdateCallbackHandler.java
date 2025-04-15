package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_GA_LANGUAGE_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_LANGUAGE_PREFERENCE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerGenAppLanguagePreferenceUpdateCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_GA_LANGUAGE_PREFERENCE);

    private final GenAppStateHelperService helperService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::triggerGaEvent
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerGaEvent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        helperService.triggerEvent(caseData, TRIGGER_GA_LANGUAGE_UPDATE);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

}
