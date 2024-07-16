package uk.gov.hmcts.reform.civil.service.defendantresponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DirectionsQuestionnairePreparer;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;

@Component
@RequiredArgsConstructor
public class DefendantResponseNonCamundaWorkFlow {

    private final DirectionsQuestionnairePreparer directionsQuestionnairePreparer;
    private final IStateFlowEngine stateFlowEngine;


    public void processWorkflow(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String userToken = (String) callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN);
        String flowState = stateFlowEngine.evaluate(caseData).getState().getName();

        //All responses received
        if (flowState.equals(DIVERGENT_RESPOND_GO_OFFLINE.fullName())
            || flowState.equals(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName())
            || flowState.equals(FULL_ADMISSION.fullName())
            || flowState.equals(PART_ADMISSION.fullName())
            || flowState.equals(COUNTER_CLAIM.fullName())
            || flowState.equals(FULL_DEFENCE.fullName())) {
            directionsQuestionnairePreparer.prepareDirectionsQuestionnaire(caseData, userToken);

        }
        else if (flowState.equals(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())) {
            directionsQuestionnairePreparer.prepareDirectionsQuestionnaire(caseData, userToken);
        }


    }

}
