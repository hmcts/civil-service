package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
public class NotificationAcknowledgedTransitionBuilder extends MidTransitionBuilder {

    public NotificationAcknowledgedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.NOTIFICATION_ACKNOWLEDGED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension))
            .moveTo(ALL_RESPONSES_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(not(respondentTimeExtension)).and(allResponsesReceived))
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(not(respondentTimeExtension))
                .and(awaitingResponsesFullDefenceReceived)
                .and(not(caseDismissedAfterClaimAcknowledged)))
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(not(respondentTimeExtension))
                .and(awaitingResponsesNonFullDefenceReceived))
            .moveTo(TAKEN_OFFLINE_BY_STAFF)
            .onlyWhen(takenOfflineByStaffAfterNotificationAcknowledged)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(caseDismissedAfterClaimAcknowledged.and(reasonNotSuitableForSdo.negate()))
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyWhen(takenOfflineSDONotDrawnAfterNotificationAcknowledged);
    }

    public static final Predicate<CaseData> takenOfflineSDONotDrawnAfterNotificationAcknowledged =
        NotificationAcknowledgedTransitionBuilder::getPredicateTakenOfflineSDONotDrawnAfterNotificationAcknowledged;

    private static boolean getPredicateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> isNotSuitableSDO(caseData)
                && caseData.getTakenOfflineDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() == null
                && caseData.getRespondent2ResponseDate() == null;
            default -> isNotSuitableSDO(caseData)
                && caseData.getTakenOfflineDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null;
        };
    }

    public static final Predicate<CaseData> takenOfflineByStaffAfterNotificationAcknowledged =
        NotificationAcknowledgedTransitionBuilder::getPredicateTakenOfflineByStaffAfterNotificationAcknowledged;

    private static boolean getPredicateTakenOfflineByStaffAfterNotificationAcknowledged(CaseData caseData) {
        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> caseData.getTakenOfflineByStaffDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() != null
                && caseData.getRespondent2TimeExtensionDate() == null
                && caseData.getRespondent2ResponseDate() == null;
            default -> caseData.getTakenOfflineByStaffDate() != null
                && caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null;
        };
    }

    public static final Predicate<CaseData> caseDismissedAfterClaimAcknowledged = caseData -> {
        LocalDateTime deadline = caseData.getClaimDismissedDeadline();
        if (deadline.isBefore(LocalDateTime.now())) {
            if (Objects.requireNonNull(getMultiPartyScenario(caseData)) == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
                return caseData.getRespondent1AcknowledgeNotificationDate() != null
                    && caseData.getRespondent2AcknowledgeNotificationDate() != null
                    && caseData.getRespondent1TimeExtensionDate() == null
                    && caseData.getRespondent2TimeExtensionDate() == null
                    && (caseData.getRespondent1ResponseDate() == null || caseData.getRespondent2ResponseDate() == null);
            }
            return caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null;
        }
        return false;
    };

    public static final Predicate<CaseData> reasonNotSuitableForSdo = caseData ->
        Objects.nonNull(caseData.getReasonNotSuitableSDO())
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput());

    private static boolean isNotSuitableSDO(CaseData caseData) {
        return caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput());
    }
}
