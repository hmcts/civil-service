package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedPastHearingFeeDue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterSDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawn;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FullDefenceProceedTransitionBuilder extends MidTransitionBuilder {

    public FullDefenceProceedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.FULL_DEFENCE_PROCEED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(IN_HEARING_READINESS, transitions).onlyWhen(isInHearingReadiness, transitions)
            .moveTo(CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE, transitions).onlyWhen(caseDismissedPastHearingFeeDue, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen((takenOfflineByStaffAfterClaimantResponseBeforeSDO
                .or(takenOfflineByStaffAfterSDO)
                .or(takenOfflineAfterNotSuitableForSdo))
                .and(not(caseDismissedPastHearingFeeDue)), transitions)
            .moveTo(TAKEN_OFFLINE_AFTER_SDO, transitions).onlyWhen(takenOfflineAfterSDO, transitions)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions).onlyWhen(takenOfflineSDONotDrawn, transitions);
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimantResponseBeforeSDO = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getApplicant1ResponseDate() != null
            && caseData.getDrawDirectionsOrderRequired() == null
            && caseData.getReasonNotSuitableSDO() == null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterSDO = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getDrawDirectionsOrderRequired() != null
            && caseData.getReasonNotSuitableSDO() == null;

    public static final Predicate<CaseData> takenOfflineAfterNotSuitableForSdo = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getDrawDirectionsOrderRequired() == null
            && caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput());
}
