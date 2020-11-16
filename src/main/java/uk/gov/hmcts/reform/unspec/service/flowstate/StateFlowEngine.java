package uk.gov.hmcts.reform.unspec.service.flowstate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.stateflow.StateFlow;
import uk.gov.hmcts.reform.unspec.stateflow.StateFlowBuilder;
import uk.gov.hmcts.reform.unspec.stateflow.model.State;

import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.applicantConfirmService;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.applicantRespondToDefence;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.applicantRespondToRequestForExtension;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimDiscontinued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimWithdrawn;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.pendingCaseIssued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentAcknowledgeService;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentAskForAnExtension;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentRespondToClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.schedulerStayClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_DISCONTINUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_STAYED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.CLAIM_WITHDRAWN;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.EXTENSION_REQUESTED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.EXTENSION_RESPONDED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FLOW_NAME;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PAYMENT_FAILED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PENDING_CASE_ISSUED;
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
                .transitionTo(PENDING_CASE_ISSUED).onlyIf(pendingCaseIssued)
            .state(PENDING_CASE_ISSUED)
                .transitionTo(PAYMENT_SUCCESSFUL).onlyIf(paymentSuccessful)
                .transitionTo(PAYMENT_FAILED).onlyIf(paymentFailed)
            .state(PAYMENT_FAILED)
                .transitionTo(PAYMENT_SUCCESSFUL).onlyIf(paymentSuccessful)
            .state(PAYMENT_SUCCESSFUL)
                .transitionTo(CLAIM_ISSUED).onlyIf(claimIssued)
            .state(CLAIM_ISSUED)
                .transitionTo(SERVICE_CONFIRMED).onlyIf(applicantConfirmService)
                .transitionTo(CLAIM_WITHDRAWN).onlyIf(claimWithdrawn)
                .transitionTo(CLAIM_DISCONTINUED).onlyIf(claimDiscontinued)
                .transitionTo(CLAIM_STAYED).onlyIf(schedulerStayClaim)
            .state(SERVICE_CONFIRMED)
                .transitionTo(SERVICE_ACKNOWLEDGED).onlyIf(respondentAcknowledgeService)
                .transitionTo(RESPONDED_TO_CLAIM).onlyIf(respondentRespondToClaim)
                .transitionTo(CLAIM_DISCONTINUED).onlyIf(claimDiscontinued)
                .transitionTo(CLAIM_WITHDRAWN).onlyIf(claimWithdrawn)
            .state(SERVICE_ACKNOWLEDGED)
                .transitionTo(EXTENSION_REQUESTED).onlyIf(respondentAskForAnExtension)
                .transitionTo(CLAIM_DISCONTINUED).onlyIf(claimDiscontinued)
                .transitionTo(CLAIM_WITHDRAWN).onlyIf(claimWithdrawn)
            .state(EXTENSION_REQUESTED)
                .transitionTo(EXTENSION_RESPONDED).onlyIf(applicantRespondToRequestForExtension)
                .transitionTo(RESPONDED_TO_CLAIM).onlyIf(respondentRespondToClaim)
                .transitionTo(CLAIM_DISCONTINUED).onlyIf(claimDiscontinued)
                .transitionTo(CLAIM_WITHDRAWN).onlyIf(claimWithdrawn)
            .state(EXTENSION_RESPONDED)
                .transitionTo(RESPONDED_TO_CLAIM).onlyIf(respondentRespondToClaim)
                .transitionTo(CLAIM_DISCONTINUED).onlyIf(claimDiscontinued)
                .transitionTo(CLAIM_WITHDRAWN).onlyIf(claimWithdrawn)
            .state(RESPONDED_TO_CLAIM)
                .transitionTo(FULL_DEFENCE).onlyIf(applicantRespondToDefence)
                .transitionTo(CLAIM_DISCONTINUED).onlyIf(claimDiscontinued)
                .transitionTo(CLAIM_WITHDRAWN).onlyIf(claimWithdrawn)
            .state(FULL_DEFENCE)
                .transitionTo(CLAIM_STAYED)
            .state(CLAIM_STAYED)
            .state(CLAIM_WITHDRAWN)
            .state(CLAIM_DISCONTINUED)
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
