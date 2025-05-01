package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

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
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PartAdmitNotSettleNoMediationTransitionBuilder extends MidTransitionBuilder {

    public PartAdmitNotSettleNoMediationTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.PART_ADMIT_NOT_SETTLED_NO_MEDIATION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(IN_HEARING_READINESS, transitions).onlyWhen(isInHearingReadiness, transitions)
            .moveTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE, transitions).onlyWhen(caseDismissedPastHearingFeeDue, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaff, transitions)
            .moveTo(TAKEN_OFFLINE_AFTER_SDO, transitions).onlyWhen(takenOfflineAfterSDO, transitions)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions).onlyWhen(takenOfflineSDONotDrawn, transitions);
    }
}
