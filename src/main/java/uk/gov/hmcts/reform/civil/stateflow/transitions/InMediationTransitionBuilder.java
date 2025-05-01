package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.MEDIATION_UNSUCCESSFUL_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InMediationTransitionBuilder extends MidTransitionBuilder {

    public InMediationTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.IN_MEDIATION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(MEDIATION_UNSUCCESSFUL_PROCEED, transitions).onlyWhen(casemanMarksMediationUnsuccessful, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaffBeforeMediationUnsuccessful, transitions);
    }

    public static final Predicate<CaseData> casemanMarksMediationUnsuccessful = caseData ->
        Objects.nonNull(caseData.getMediation().getUnsuccessfulMediationReason())
            || (Objects.nonNull(caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect())
            && !caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect().isEmpty());

    public static final Predicate<CaseData> takenOfflineByStaffBeforeMediationUnsuccessful = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && (Objects.isNull(caseData.getMediation().getUnsuccessfulMediationReason())
            && Objects.isNull(caseData.getMediation().getMediationUnsuccessfulReasonsMultiSelect()));
}
