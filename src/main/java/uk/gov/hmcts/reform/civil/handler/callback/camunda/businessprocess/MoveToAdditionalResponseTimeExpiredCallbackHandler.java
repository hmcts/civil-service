package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ADDITIONAL_RESPONSE_TIME_EXPIRED;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoveToAdditionalResponseTimeExpiredCallbackHandler extends CallbackHandler {

    private final ParentCaseUpdateHelper parentCaseUpdateHelper;

    private static final List<CaseEvent> EVENTS = singletonList(CHANGE_STATE_TO_ADDITIONAL_RESPONSE_TIME_EXPIRED);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::changeApplicationState,
            callbackKey(SUBMITTED), this::changeGADetailsStatusInParent
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse changeApplicationState(CallbackParams callbackParams) {
        Long caseId = callbackParams.getCaseData().getCcdCaseReference();
        log.info("Changing state to APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION for caseId: {}", caseId);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(ADDITIONAL_RESPONSE_TIME_EXPIRED.toString())
            .build();
    }

    private CallbackResponse changeGADetailsStatusInParent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Updating parent with latest state of application-caseId: {}", caseData.getCcdCaseReference());
        parentCaseUpdateHelper.updateParentWithGAState(
            caseData,
            ADDITIONAL_RESPONSE_TIME_EXPIRED.getDisplayedValue()
        );

        return SubmittedCallbackResponse.builder().build();
    }
}
