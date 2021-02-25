package uk.gov.hmcts.reform.unspec.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.stateflow.StateFlow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ACKNOWLEDGE_SERVICE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MOVE_CLAIM_TO_STRUCK_OUT;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MOVE_TO_STAYED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.WITHDRAW_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.AWAITING_CASE_NOTIFICATION;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_STAYED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.RESPONDED_TO_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.SERVICE_ACKNOWLEDGED;

@Service
@RequiredArgsConstructor
public class FlowStateAllowedEventService {

    private final StateFlowEngine stateFlowEngine;

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE = Map.of(
        DRAFT.fullName(),
        List.of(CREATE_CLAIM, WITHDRAW_CLAIM, DISCONTINUE_CLAIM),

        AWAITING_CASE_NOTIFICATION.fullName(),
        List.of(
            NOTIFY_DEFENDANT_OF_CLAIM,
            ADD_DEFENDANT_LITIGATION_FRIEND,
            CASE_PROCEEDS_IN_CASEMAN,
            ADD_OR_AMEND_CLAIM_DOCUMENTS
        ),

        AWAITING_CASE_DETAILS_NOTIFICATION.fullName(),
        List.of(
            NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
            ADD_DEFENDANT_LITIGATION_FRIEND,
            CASE_PROCEEDS_IN_CASEMAN,
            ADD_OR_AMEND_CLAIM_DOCUMENTS
        ),

        CLAIM_ISSUED.fullName(),
        List.of(MOVE_TO_STAYED, ACKNOWLEDGE_SERVICE, ADD_DEFENDANT_LITIGATION_FRIEND, WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM, CASE_PROCEEDS_IN_CASEMAN
        ),

        CLAIM_STAYED.fullName(),
        List.of(WITHDRAW_CLAIM, DISCONTINUE_CLAIM),

        SERVICE_ACKNOWLEDGED.fullName(),
        List.of(DEFENDANT_RESPONSE, ADD_DEFENDANT_LITIGATION_FRIEND, WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM, CASE_PROCEEDS_IN_CASEMAN),

        RESPONDED_TO_CLAIM.fullName(),
        List.of(CLAIMANT_RESPONSE, WITHDRAW_CLAIM, ADD_DEFENDANT_LITIGATION_FRIEND, DISCONTINUE_CLAIM,
                MOVE_CLAIM_TO_STRUCK_OUT, CASE_PROCEEDS_IN_CASEMAN
        ),

        FULL_DEFENCE.fullName(),
        List.of(ADD_DEFENDANT_LITIGATION_FRIEND, WITHDRAW_CLAIM, DISCONTINUE_CLAIM, CASE_PROCEEDS_IN_CASEMAN)
    );

    public FlowState getFlowState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return FlowState.fromFullName(stateFlow.getState().getName());
    }

    public List<CaseEvent> getAllowedEvents(String stateFullName) {
        return ALLOWED_EVENTS_ON_FLOW_STATE.getOrDefault(stateFullName, emptyList());
    }

    public boolean isAllowedOnState(String stateFullName, CaseEvent caseEvent) {
        return ALLOWED_EVENTS_ON_FLOW_STATE
            .getOrDefault(stateFullName, emptyList())
            .contains(caseEvent);
    }

    public boolean isAllowed(CaseDetails caseDetails, CaseEvent caseEvent) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseDetails);
        return isAllowedOnState(stateFlow.getState().getName(), caseEvent);
    }

    public List<String> getAllowedStates(CaseEvent caseEvent) {
        return ALLOWED_EVENTS_ON_FLOW_STATE.entrySet().stream()
            .filter(entry -> entry.getValue().contains(caseEvent))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
