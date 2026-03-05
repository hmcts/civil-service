package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.service.Time;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.NON_LIVE_STATES;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAIN_CASE_CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_CLOSED;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainCaseClosedEventCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private final ObjectMapper objectMapper;
    private final Time time;

    private static final List<CaseEvent> MAIN_CASE_CLOSED_EVENTS = singletonList(MAIN_CASE_CLOSED);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::changeApplicationStateToApplicationClosed
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return MAIN_CASE_CLOSED_EVENTS;
    }

    private CallbackResponse changeApplicationStateToApplicationClosed(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        Long caseId = caseData.getCcdCaseReference();

        if (!NON_LIVE_STATES.contains(caseData.getCcdState())) {
            log.info("Changing state to APPLICATION_CLOSED for caseId: {}", caseId);

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder
                .businessProcess(
                    new BusinessProcess()
                        .setCamundaEvent(MAIN_CASE_CLOSED.name())
                        .setStatus(BusinessProcessStatus.FINISHED))
                .applicationClosedDate(time.now());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .state(APPLICATION_CLOSED.toString())
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
        } else {
            return emptyCallbackResponse(callbackParams);
        }
    }
}
