package uk.gov.hmcts.reform.unspec.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.stateflow.StateFlow;
import uk.gov.hmcts.reform.unspec.stateflow.StateFlowBuilder;
import uk.gov.hmcts.reform.unspec.stateflow.model.State;

import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimantConfirmService;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimantIssueClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimantRespondToDefence;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimantRespondToRequestForExtension;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.defendantAcknowledgeService;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.defendantAskForAnExtension;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.defendantRespondToClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.schedulerStayClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_STAYED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.EXTENSION_REQUESTED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.EXTENSION_RESPONDED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FLOW_NAME;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.RESPONDED_TO_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.SERVICE_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.SERVICE_CONFIRMED;

@Component
@RequiredArgsConstructor
public class StateFlowEngine {

    private final CaseDetailsConverter caseDetailsConverter;

    public StateFlow build() {
        return StateFlowBuilder.<FlowState.Main>flow(FLOW_NAME)
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

    public boolean hasTransitionedTo(CaseDetails caseDetails, FlowState.Main state) {
        return evaluate(caseDetails).getStateHistory().stream()
            .map(State::getName)
            .anyMatch(name -> name.equals(state.fullName()));
    }
}
