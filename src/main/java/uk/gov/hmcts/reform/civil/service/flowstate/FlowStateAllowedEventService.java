package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_PARTY_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESUBMIT_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.WITHDRAW_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DRAFT;

@Service
@RequiredArgsConstructor
public class FlowStateAllowedEventService {

    private final StateFlowEngine stateFlowEngine;

    private final CaseDetailsConverter caseDetailsConverter;

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE = Map.ofEntries(
        entry(
            DRAFT.fullName(),
            List.of(
                CREATE_CLAIM
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
            List.of(
                RESUBMIT_CLAIM,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            CLAIM_ISSUED.fullName(),
            List.of(
                NOTIFY_DEFENDANT_OF_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_OR_AMEND_CLAIM_DOCUMENTS,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM,
                DISCONTINUE_CLAIM,
                WITHDRAW_CLAIM
            )
        ),

        entry(
            CLAIM_NOTIFIED.fullName(),
            List.of(
                NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_OR_AMEND_CLAIM_DOCUMENTS,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM,
                DISCONTINUE_CLAIM,
                WITHDRAW_CLAIM
            )
        ),

        entry(
            CLAIM_DETAILS_NOTIFIED.fullName(),
            List.of(
                ACKNOWLEDGE_CLAIM,
                DEFENDANT_RESPONSE,
                INFORM_AGREED_EXTENSION_DATE,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                AMEND_PARTY_DETAILS,
                CASE_PROCEEDS_IN_CASEMAN,
                DISMISS_CLAIM
            )
        ),

        entry(
            CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION.fullName(),
            List.of(
                ACKNOWLEDGE_CLAIM,
                DEFENDANT_RESPONSE,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM
            )
        ),

        entry(
            NOTIFICATION_ACKNOWLEDGED.fullName(),
            List.of(
                DEFENDANT_RESPONSE,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                INFORM_AGREED_EXTENSION_DATE,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM
            )
        ),

        entry(
            NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
            List.of(
                DEFENDANT_RESPONSE,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM
            )
        ),

        entry(
            FULL_DEFENCE.fullName(),
            List.of(
                CLAIMANT_RESPONSE,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                TAKE_CASE_OFFLINE
            )
        ),

        entry(
            FULL_ADMISSION.fullName(),
            List.of(
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            PART_ADMISSION.fullName(),
            List.of(
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            COUNTER_CLAIM.fullName(),
            List.of(
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            FULL_DEFENCE_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            FULL_DEFENCE_NOT_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
            List.of(CASE_PROCEEDS_IN_CASEMAN)
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
            List.of(CASE_PROCEEDS_IN_CASEMAN)
        ),
        entry(
            PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(TAKE_CASE_OFFLINE)
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        )
    );

    private static final Map<String, List<CaseEvent>> ALLOWED_EVENTS_ON_FLOW_STATE_SPEC = Map.ofEntries(
        entry(
            DRAFT.fullName(),
            List.of(
                CREATE_CLAIM
            )
        ),

        entry(
            SPEC_DRAFT.fullName(),
            List.of(
                CREATE_CLAIM_SPEC
            )
        ),

        entry(
            CLAIM_ISSUED_PAYMENT_FAILED.fullName(),
            List.of(
                RESUBMIT_CLAIM,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            CLAIM_ISSUED.fullName(),
            List.of(
                NOTIFY_DEFENDANT_OF_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_OR_AMEND_CLAIM_DOCUMENTS,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM,
                DISCONTINUE_CLAIM,
                WITHDRAW_CLAIM
            )
        ),
        entry(
            CLAIM_NOTIFIED.fullName(),
            List.of(
                ACKNOWLEDGEMENT_OF_SERVICE,
                INFORM_AGREED_EXTENSION_DATE,
                INFORM_AGREED_EXTENSION_DATE_SPEC,
                DEFENDANT_RESPONSE_SPEC,
                NOTIFY_DEFENDANT_OF_CLAIM_DETAILS,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                CASE_PROCEEDS_IN_CASEMAN,
                ADD_OR_AMEND_CLAIM_DOCUMENTS,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM,
                DISCONTINUE_CLAIM,
                WITHDRAW_CLAIM
            )
        ),
        entry(
            NOTIFICATION_ACKNOWLEDGED.fullName(),
            List.of(
                DEFENDANT_RESPONSE,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                INFORM_AGREED_EXTENSION_DATE,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM
            )
        ),

        entry(
            NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION.fullName(),
            List.of(
                DEFENDANT_RESPONSE,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                DISMISS_CLAIM
            )
        ),

        entry(
            FULL_DEFENCE.fullName(),
            List.of(
                CLAIMANT_RESPONSE,
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS,
                TAKE_CASE_OFFLINE
            )
        ),

        entry(
            FULL_ADMISSION.fullName(),
            List.of(
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            PART_ADMISSION.fullName(),
            List.of(
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            COUNTER_CLAIM.fullName(),
            List.of(
                WITHDRAW_CLAIM,
                ADD_DEFENDANT_LITIGATION_FRIEND,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            FULL_DEFENCE_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),

        entry(
            FULL_DEFENCE_NOT_PROCEED.fullName(),
            List.of(
                ADD_DEFENDANT_LITIGATION_FRIEND,
                WITHDRAW_CLAIM,
                DISCONTINUE_CLAIM,
                CASE_PROCEEDS_IN_CASEMAN,
                AMEND_PARTY_DETAILS
            )
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName(),
            List.of(CASE_PROCEEDS_IN_CASEMAN)
        ),
        entry(
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName(),
            List.of(CASE_PROCEEDS_IN_CASEMAN)
        ),
        entry(
            PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(TAKE_CASE_OFFLINE)
        ),
        entry(
            PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        ),
        entry(
            PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        ),
        entry(
            PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA.fullName(),
            List.of(DISMISS_CLAIM)
        )
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

    public boolean isAllowedOnStateForSpec(String stateFullName, CaseEvent caseEvent) {

        return ALLOWED_EVENTS_ON_FLOW_STATE_SPEC
            .getOrDefault(stateFullName, emptyList())
            .contains(caseEvent);

    }

    public boolean isAllowed(CaseDetails caseDetails, CaseEvent caseEvent) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        if ((caseData.getSuperClaimType() != null && caseData.getSuperClaimType().equals(SPEC_CLAIM))
            || caseEvent.toString().equals("CREATE_CLAIM_SPEC")) {
            StateFlow stateFlow = stateFlowEngine.evaluateSpec(caseDetails);
            return isAllowedOnStateForSpec(stateFlow.getState().getName(), caseEvent);
        } else {
            StateFlow stateFlow = stateFlowEngine.evaluate(caseDetails);
            return isAllowedOnState(stateFlow.getState().getName(), caseEvent);
        }

    }

    public List<String> getAllowedStates(CaseEvent caseEvent) {
        if (caseEvent.toString().equals("CREATE_CLAIM_SPEC")) {
            return ALLOWED_EVENTS_ON_FLOW_STATE_SPEC.entrySet().stream()
                .filter(entry -> entry.getValue().contains(caseEvent))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
        return ALLOWED_EVENTS_ON_FLOW_STATE.entrySet().stream()
            .filter(entry -> entry.getValue().contains(caseEvent))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
