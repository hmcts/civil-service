package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedPastHearingFeeDue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
public class MediationUnsuccessfulProceedTransitionBuilder extends MidTransitionBuilder {
    public MediationUnsuccessfulProceedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.MEDIATION_UNSUCCESSFUL_PROCEED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(IN_HEARING_READINESS).onlyWhen(isInHearingReadiness)
            .moveTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE).onlyWhen(caseDismissedPastHearingFeeDue)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaff)
            .moveTo(TAKEN_OFFLINE_AFTER_SDO).onlyWhen(takenOfflineAfterSDO)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyWhen(takenOfflineSDONotDrawn);
    }
}
