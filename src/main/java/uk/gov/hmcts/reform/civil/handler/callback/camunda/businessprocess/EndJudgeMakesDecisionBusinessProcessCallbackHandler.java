package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.JudicialDecisionHelper;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.service.StateGeneratorService;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_JUDGE_BUSINESS_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndJudgeMakesDecisionBusinessProcessCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(END_JUDGE_BUSINESS_PROCESS_GASPEC);

    private final ParentCaseUpdateHelper parentCaseUpdateHelper;
    private final StateGeneratorService stateGeneratorService;
    private final JudicialDecisionHelper judicialDecisionHelper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::endJudgeBusinessProcess);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse endJudgeBusinessProcess(CallbackParams callbackParams) {
        log.info("End judge makes decision business process for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());
        CaseData data = callbackParams.getCaseData();
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        Objects.requireNonNull(gaCaseData, "gaCaseData must be present on CallbackParams");
        CaseState newState = getNewStateDependingOn(data);
        if (isApplicationMakeVisibleToDefendant(data)
            || (data.getIsGaRespondentOneLip() == YesOrNo.YES && newState != APPLICATION_ADD_PAYMENT)) {
            parentCaseUpdateHelper.updateParentApplicationVisibilityWithNewState(
                gaCaseData, newState.getDisplayedValue());
        } else {
            parentCaseUpdateHelper.updateParentWithGAState(gaCaseData, newState.getDisplayedValue());
        }

        return evaluateReady(callbackParams, newState);
    }

    private CaseState getNewStateDependingOn(CaseData data) {
        return stateGeneratorService.getCaseStateForEndJudgeBusinessProcess(data);
    }

    private CallbackResponse evaluateReady(CallbackParams callbackParams,
                                           CaseState newState) {
        Map<String, Object> output = callbackParams.getRequest().getCaseDetails().getData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(newState.toString())
            .data(output)
            .build();
    }

    private boolean isApplicationMakeVisibleToDefendant(CaseData caseData) {
        return judicialDecisionHelper.isOrderMakeDecisionMadeVisibleToDefendant(caseData);
    }
}
