package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullAdmitReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceOrFullAdmitReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
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
public class NotificationAcknowledgedTransitionBuilder extends MidTransitionBuilder {

    public NotificationAcknowledgedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.NOTIFICATION_ACKNOWLEDGED, featureToggleService);
    }

    @Override
    void setUpTransitions(List<Transition> transitions) {
        this.moveTo(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, transitions)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension), transitions)
            .moveTo(ALL_RESPONSES_RECEIVED, transitions)
            .onlyWhen(notificationAcknowledged.and(not(respondentTimeExtension)).and(allResponsesReceived), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, transitions)
            .onlyWhen(notificationAcknowledged.and(not(respondentTimeExtension))
                .and(awaitingResponsesFullDefenceReceived)
                .and(not(caseDismissedAfterClaimAcknowledged)), transitions)
            .moveTo(AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(notificationAcknowledged.and(not(respondentTimeExtension))
                          .and(awaitingResponsesFullAdmitReceived), transitions)
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED, transitions)
            .onlyWhen(notificationAcknowledged.and(not(respondentTimeExtension))
                .and(awaitingResponsesNonFullDefenceOrFullAdmitReceived), transitions)
            .moveTo(TAKEN_OFFLINE_BY_STAFF, transitions)
            .onlyWhen(takenOfflineByStaffAfterNotificationAcknowledged, transitions)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA, transitions)
            .onlyWhen(caseDismissedAfterClaimAcknowledged.and(reasonNotSuitableForSdo.negate()), transitions)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN, transitions).onlyWhen(takenOfflineSDONotDrawnAfterNotificationAcknowledged, transitions);
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
                    && (caseData.getRespondent1ResponseDate() == null || caseData.getRespondent2ResponseDate() == null)
                    && caseData.getTakenOfflineByStaffDate() == null;
            }
            return caseData.getRespondent1AcknowledgeNotificationDate() != null
                && caseData.getRespondent1TimeExtensionDate() == null
                && caseData.getRespondent1ResponseDate() == null
                && caseData.getTakenOfflineByStaffDate() == null;
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
