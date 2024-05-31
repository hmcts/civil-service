package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOffline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.divergentRespondWithDQAndGoOfflineSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefence;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
public class AllResponsesReceivedTransitionBuilder extends MidTransitionBuilder {

    public AllResponsesReceivedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.ALL_RESPONSES_RECEIVED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(FULL_DEFENCE).onlyWhen(fullDefence)
            .moveTo(FULL_DEFENCE).onlyWhen(fullDefenceSpec)
            .moveTo(FULL_ADMISSION).onlyWhen(fullAdmission.and(not(divergentRespondGoOffline)))
            .moveTo(PART_ADMISSION).onlyWhen(partAdmission.and(not(divergentRespondGoOffline)))
            .moveTo(COUNTER_CLAIM).onlyWhen(counterClaim.and(not(divergentRespondGoOffline)))
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(divergentRespondGoOffline)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(divergentRespondGoOfflineSpec)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(fullAdmissionSpec)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(partAdmissionSpec)
            .moveTo(DIVERGENT_RESPOND_GO_OFFLINE).onlyWhen(counterClaimSpec)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE).onlyWhen(divergentRespondWithDQAndGoOffline)
            .moveTo(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE).onlyWhen(divergentRespondWithDQAndGoOfflineSpec)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaffAfterClaimDetailsNotified)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA).onlyWhen(caseDismissedAfterDetailNotified);
    }
}
