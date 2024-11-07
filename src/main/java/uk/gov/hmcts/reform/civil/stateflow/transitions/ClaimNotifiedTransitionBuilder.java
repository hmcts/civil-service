package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.hasNotifiedClaimDetailsToBoth;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimNotifiedTransitionBuilder extends MidTransitionBuilder {

    public ClaimNotifiedTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_NOTIFIED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(CLAIM_DETAILS_NOTIFIED, transitions).onlyWhen(claimDetailsNotified, transitions)
            .moveTo(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED, transitions).onlyWhen(takenOfflineAfterClaimDetailsNotified, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaffAfterClaimNotified, transitions)
            .moveTo(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(pastClaimDetailsNotificationDeadline, transitions);
    }

    public static final Predicate<CaseData> claimDetailsNotified = caseData ->
        !SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getClaimDetailsNotificationDate() != null
            && (caseData.getDefendantSolicitorNotifyClaimDetailsOptions() == null
            || hasNotifiedClaimDetailsToBoth.test(caseData));
}
