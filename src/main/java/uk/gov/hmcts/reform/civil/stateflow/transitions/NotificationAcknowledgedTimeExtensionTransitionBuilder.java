package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullAdmitReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NotificationAcknowledgedTimeExtensionTransitionBuilder extends MidTransitionBuilder {

    public NotificationAcknowledgedTimeExtensionTransitionBuilder(FeatureToggleService featureToggleService) {
        super(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(ALL_RESPONSES_RECEIVED, transitions)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension).and(allResponsesReceived).and(claimDismissalOutOfTime.negate())
                .and(takenOfflineByStaff.negate()), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, transitions)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension)
                .and(awaitingResponsesFullDefenceReceived)
                .and(not(caseDismissedAfterClaimAcknowledgedExtension)), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension)
                          .and(awaitingResponsesFullAdmitReceived), transitions)
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension)
                .and(awaitingResponsesNonFullDefenceOrFullAdmitReceived), transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension, transitions)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(caseDismissedAfterClaimAcknowledgedExtension, transitions)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions)
            .onlyWhen(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension, transitions);
    }

    public static final Predicate<CaseData> takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension =
        NotificationAcknowledgedTimeExtensionTransitionBuilder::getPredicateTakenOfflineSDONotDrawnAfterNotificationAckTimeExt;

    private static boolean getPredicateTakenOfflineSDONotDrawnAfterNotificationAckTimeExt(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> isNotSuitableSDO(caseData)
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() != null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() != null
                && caseData.getRespondent2ResponseDate() == null;
            default -> isNotSuitableSDO(caseData)
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() != null
                && caseData.getRespondent1ResponseDate() == null;
        };
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension =
        NotificationAcknowledgedTimeExtensionTransitionBuilder::getPredicateTakenOfflineByStaffAfterNotificationAckTimeExt;

    private static boolean getPredicateTakenOfflineByStaffAfterNotificationAckTimeExt(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> caseData.getTakenOfflineByStaffDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() != null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() != null;
            default -> caseData.getTakenOfflineByStaffDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() != null;
        };
    }

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledgedExtension = caseData -> {
        LocalDateTime deadline = caseData.getClaimDismissedDeadline();
        if (deadline.isBefore(LocalDateTime.now())) {
            switch (getMultiPartyScenario(caseData)) {
                case ONE_V_TWO_TWO_LEGAL_REP:
                    return caseData.getRespondent1AcknowledgeNotificationDate() != null
                        && caseData.getRespondent2AcknowledgeNotificationDate() != null
                        && (caseData.getRespondent1TimeExtensionDate() != null || caseData.getRespondent2TimeExtensionDate() != null)
                        && caseData.getReasonNotSuitableSDO() == null
                        && caseData.getTakenOfflineByStaffDate() == null;
                default:
                    return caseData.getRespondent1AcknowledgeNotificationDate() != null
                        && caseData.getRespondent1TimeExtensionDate() != null
                        && caseData.getReasonNotSuitableSDO() == null
                        && caseData.getTakenOfflineByStaffDate() == null;
            }
        }
        return false;
    };

    public static final Predicate<CaseData> claimDismissalOutOfTime = caseData ->
        caseData.getClaimDismissedDeadline() != null
            && caseData.getClaimDismissedDeadline().isBefore(LocalDateTime.now());

    private static boolean isNotSuitableSDO(CaseData caseData) {
        return caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
            && caseData.getTakenOfflineDate() != null;
    }
}
