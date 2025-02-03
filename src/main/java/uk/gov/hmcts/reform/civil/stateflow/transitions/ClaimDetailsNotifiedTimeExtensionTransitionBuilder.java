package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.apache.commons.lang.StringUtils;
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

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.responseDeadlinePassed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NO_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimDetailsNotifiedTimeExtensionTransitionBuilder extends MidTransitionBuilder {

    public ClaimDetailsNotifiedTimeExtensionTransitionBuilder(
        FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(NOTIFICATION_ACKNOWLEDGED, transitions).onlyWhen(notificationAcknowledged, transitions)
            .moveTo(ALL_RESPONSES_RECEIVED, transitions).onlyWhen((respondentTimeExtension).and(allResponsesReceived), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, transitions)
            .onlyWhen((awaitingResponsesFullDefenceReceived).and(respondentTimeExtension)
                .and(not(caseDismissedAfterDetailNotifiedExtension)), transitions)
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED, transitions)
            .onlyWhen((awaitingResponsesNonFullDefenceReceived).and(respondentTimeExtension), transitions)
            .moveTo(NO_DEFENDANT_RESPONSE, transitions)
            .onlyWhen(respondentTimeExtension.and(responseDeadlinePassed), transitions)
            .moveTo(IN_HEARING_READINESS, transitions).onlyWhen(isInHearingReadiness, transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions).onlyWhen(takenOfflineByStaffAfterClaimDetailsNotifiedExtension, transitions)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(caseDismissedAfterDetailNotifiedExtension, transitions)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions).onlyWhen(takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension, transitions);
    }

    public static final Predicate<CaseData> takenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension = caseData ->
        caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
            && caseData.getTakenOfflineDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> takenOfflineByStaffAfterClaimDetailsNotifiedExtension = caseData ->
        caseData.getTakenOfflineByStaffDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent1ResponseDate() == null;

    public static final Predicate<CaseData> caseDismissedAfterDetailNotifiedExtension = caseData ->
        caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now())
            && ((caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1TimeExtensionDate() != null)
            || (caseData.getRespondent2AcknowledgeNotificationDate() == null
            && caseData.getRespondent2TimeExtensionDate() != null))
            && caseData.getRespondent1ClaimResponseIntentionType() == null
            && caseData.getRespondent2ClaimResponseIntentionType() == null;
}
