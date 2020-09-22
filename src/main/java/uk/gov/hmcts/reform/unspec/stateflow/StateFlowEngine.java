package uk.gov.hmcts.reform.unspec.stateflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.stateflow.model.State;

import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.CLAIM_STAYED;
import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.DRAFT;
import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.EXTENSION_REQUESTED;
import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.EXTENSION_RESPONDED;
import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.RESPONDED_TO_CLAIM;
import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.SERVICE_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.unspec.stateflow.StateFlowEngine.FlowState.SERVICE_CONFIRMED;
import static uk.gov.hmcts.reform.unspec.stateflow.utils.PredicateUtils.claimantConfirmService;
import static uk.gov.hmcts.reform.unspec.stateflow.utils.PredicateUtils.claimantIssueClaim;
import static uk.gov.hmcts.reform.unspec.stateflow.utils.PredicateUtils.claimantRespondToDefence;
import static uk.gov.hmcts.reform.unspec.stateflow.utils.PredicateUtils.claimantRespondToRequestForExtension;
import static uk.gov.hmcts.reform.unspec.stateflow.utils.PredicateUtils.defendantAcknowledgeService;
import static uk.gov.hmcts.reform.unspec.stateflow.utils.PredicateUtils.defendantAskForAnExtension;
import static uk.gov.hmcts.reform.unspec.stateflow.utils.PredicateUtils.defendantRespondToClaim;
import static uk.gov.hmcts.reform.unspec.stateflow.utils.PredicateUtils.schedulerStayClaim;

@Component
@RequiredArgsConstructor
public class StateFlowEngine {

    public static final String FLOW_NAME = "MAIN";
    private final CaseDetailsConverter caseDetailsConverter;

    public enum FlowState {
        DRAFT,
        CLAIM_ISSUED,
        CLAIM_STAYED,
        SERVICE_CONFIRMED,
        SERVICE_ACKNOWLEDGED,
        EXTENSION_REQUESTED,
        EXTENSION_RESPONDED,
        RESPONDED_TO_CLAIM,
        FULL_DEFENCE;

        public String fullName() {
            return FLOW_NAME + "." + name();
        }
    }

    public StateFlow build() {
        return StateFlowBuilder.<StateFlowEngine.FlowState>flow(FLOW_NAME)
            .initial(DRAFT)
            .transitionTo(CLAIM_ISSUED).onlyIf(claimantIssueClaim)
            .state(CLAIM_ISSUED)
            .transitionTo(SERVICE_CONFIRMED).onlyIf(claimantConfirmService)
            .transitionTo(CLAIM_STAYED).onlyIf(schedulerStayClaim)
            .state(CLAIM_STAYED)
            .state(SERVICE_CONFIRMED)
            .transitionTo(SERVICE_ACKNOWLEDGED).onlyIf(defendantAcknowledgeService)
            .transitionTo(RESPONDED_TO_CLAIM).onlyIf(defendantRespondToClaim)
            .state(SERVICE_ACKNOWLEDGED)
            .transitionTo(EXTENSION_REQUESTED).onlyIf(defendantAskForAnExtension)
            .state(EXTENSION_REQUESTED)
            .transitionTo(EXTENSION_RESPONDED).onlyIf(claimantRespondToRequestForExtension)
            .transitionTo(RESPONDED_TO_CLAIM).onlyIf(defendantRespondToClaim)
            .state(EXTENSION_RESPONDED)
            .transitionTo(RESPONDED_TO_CLAIM).onlyIf(defendantRespondToClaim)
            .state(RESPONDED_TO_CLAIM)
            .transitionTo(FULL_DEFENCE).onlyIf(claimantRespondToDefence)
            .state(FULL_DEFENCE)
            .build();
    }

    public StateFlow evaluate(CaseDetails caseDetails) {
        return evaluate(caseDetailsConverter.toCaseData(caseDetails));
    }

    public StateFlow evaluate(CaseData caseData) {
        return build().evaluate(caseData);
    }

    public boolean hasTransitionedTo(CaseDetails caseDetails, FlowState state) {
        return evaluate(caseDetails).getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name -> name.equals(state.fullName()));
    }
}
