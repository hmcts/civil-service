package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NoDefendantResponseTransitionBuilder extends MidTransitionBuilder {

    public NoDefendantResponseTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.NO_DEFENDANT_RESPONSE, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(IN_HEARING_READINESS, transitions).onlyWhen(isInHearingReadiness, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaffAfterNoResponse, transitions)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, transitions).onlyWhen(caseDismissedAfterDetailNotifiedExtension, transitions);
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterNoResponse = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDateTime.now());

    public static final Predicate<CaseData> caseDismissedAfterDetailNotifiedExtension = caseData ->
        caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getRespondent1ResponseDeadline().isBefore(LocalDateTime.now());
}
