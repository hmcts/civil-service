package uk.gov.hmcts.reform.civil.stateflow.transitions;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.HearingPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

public abstract class MidTransitionBuilder extends TransitionBuilder {

    protected static final Predicate<CaseData> TAKEN_OFFLINE_BY_SYSTEM_AFTER_SDO =
        TakenOfflinePredicate.byStaff.negate()
            .and(TakenOfflinePredicate.afterSdo)
            .and(TakenOfflinePredicate.bySystem);

    protected static final Predicate<CaseData> TAKEN_OFFLINE_SDO_NOT_DRAWN_NOT_BY_STAFF =
        TakenOfflinePredicate.byStaff.negate().and(TakenOfflinePredicate.sdoNotDrawn);

    public MidTransitionBuilder(FlowState.Main fromState, FeatureToggleService featureToggleService) {
        super(fromState, featureToggleService);
    }

    protected void addHearingReadinessTransitions(
        List<Transition> transitions,
        Predicate<CaseData> claimDismissedHearingFeeDueCondition,
        Predicate<CaseData> takenOfflineByStaffCondition
    ) {
        this.moveTo(IN_HEARING_READINESS, transitions)
            .onlyWhen(HearingPredicate.isInReadiness, transitions)

            .moveTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE, transitions)
            .onlyWhen(claimDismissedHearingFeeDueCondition, transitions)

            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(takenOfflineByStaffCondition, transitions)

            .moveTo(TAKEN_OFFLINE_AFTER_SDO, transitions)
            .onlyWhen(TAKEN_OFFLINE_BY_SYSTEM_AFTER_SDO, transitions)

            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions)
            .onlyWhen(TAKEN_OFFLINE_SDO_NOT_DRAWN_NOT_BY_STAFF, transitions);
    }
}
