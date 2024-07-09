package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
public class NotificationAcknowledgedTimeExtensionTransitionBuilder extends MidTransitionBuilder {

    public NotificationAcknowledgedTimeExtensionTransitionBuilder(FeatureToggleService featureToggleService) {
        super(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(ALL_RESPONSES_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension).and(allResponsesReceived).and(claimDismissalOutOfTime.negate())
                .and(takenOfflineByStaff.negate()))
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension)
                .and(awaitingResponsesFullDefenceReceived)
                .and(not(caseDismissedAfterClaimAcknowledgedExtension)))
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension)
                .and(awaitingResponsesNonFullDefenceReceived))
            .moveTo(TAKEN_OFFLINE_BY_STAFF)
            .onlyWhen(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(caseDismissedAfterClaimAcknowledgedExtension)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyWhen(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension);
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
