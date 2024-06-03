package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedPastHearingFeeDue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.specSmallClaimCarm;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterNotSuitableForSdo;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimantResponseBeforeSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
public class FullDefenceProceedTransitionBuilder extends MidTransitionBuilder {

    public FullDefenceProceedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_DEFENCE_PROCEED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(IN_HEARING_READINESS).onlyWhen(isInHearingReadiness)
            .moveTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE).onlyWhen(caseDismissedPastHearingFeeDue)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen((takenOfflineByStaffAfterClaimantResponseBeforeSDO
                .or(takenOfflineByStaffAfterSDO)
                .or(takenOfflineAfterNotSuitableForSdo))
                .and(not(caseDismissedPastHearingFeeDue)))
            .moveTo(TAKEN_OFFLINE_AFTER_SDO).onlyWhen(takenOfflineAfterSDO)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyWhen(takenOfflineSDONotDrawn)
            .moveTo(IN_MEDIATION).onlyWhen(specSmallClaimCarm);
    }
}
