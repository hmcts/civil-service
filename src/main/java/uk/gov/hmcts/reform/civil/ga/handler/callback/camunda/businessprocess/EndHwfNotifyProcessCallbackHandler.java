package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_GA_HWF_NOTIFY_PROCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndHwfNotifyProcessCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(END_GA_HWF_NOTIFY_PROCESS);

    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::endHwfNotifyBusinessProcess);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse endHwfNotifyBusinessProcess(CallbackParams callbackParams) {
        log.info("End Hwf notify business process for caseId: {}",
                 callbackParams.getGeneralApplicationCaseData().getCcdCaseReference());
        return evaluateReady(callbackParams);
    }

    private CallbackResponse evaluateReady(CallbackParams callbackParams) {
        Map<String, Object> output = callbackParams.getRequest().getCaseDetails().getData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(output)
            .build();
    }
}
