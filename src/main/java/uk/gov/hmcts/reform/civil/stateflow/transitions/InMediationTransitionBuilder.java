package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.casemanMarksMediationUnsuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffBeforeMediationUnsuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.MEDIATION_UNSUCCESSFUL_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
public class InMediationTransitionBuilder extends MidTransitionBuilder {

    public InMediationTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.IN_MEDIATION, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(MEDIATION_UNSUCCESSFUL_PROCEED).onlyWhen(casemanMarksMediationUnsuccessful)
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaffBeforeMediationUnsuccessful);
    }
}
