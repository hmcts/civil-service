package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ClaimDetailsNotifiedTransitionBuilder extends MidTransitionBuilder {

    public ClaimDetailsNotifiedTransitionBuilder(FeatureToggleService featureToggleService) {
        super(FlowState.Main.CLAIM_DETAILS_NOTIFIED, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION)
            .onlyWhen(respondentTimeExtension
                          .and(not(notificationAcknowledged))
                          .and(not(isInHearingReadiness)))
            //Acknowledging Claim First
            .moveTo(NOTIFICATION_ACKNOWLEDGED).onlyWhen(notificationAcknowledged.and(not(isInHearingReadiness)))
            //Direct Response, without Acknowledging
            .moveTo(ALL_RESPONSES_RECEIVED)
            .onlyWhen(allResponsesReceived.and(not(notificationAcknowledged)).and(not(respondentTimeExtension)).and(not(isInHearingReadiness)))
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
            .onlyWhen(awaitingResponsesFullDefenceReceived
                          .and(not(notificationAcknowledged)).and(not(respondentTimeExtension))
                          .and(not(caseDismissedAfterDetailNotified)))
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
            .onlyWhen(awaitingResponsesNonFullDefenceReceived
                          .and(not(notificationAcknowledged)).and(not(respondentTimeExtension)))
            .moveTo(TAKEN_OFFLINE_BY_STAFF).onlyWhen(takenOfflineByStaffAfterClaimDetailsNotified)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(caseDismissedAfterDetailNotified)
            .moveTo(IN_HEARING_READINESS).onlyWhen(isInHearingReadiness)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyWhen(takenOfflineSDONotDrawnAfterClaimDetailsNotified);
    }

    public static final Predicate<CaseData> takenOfflineSDONotDrawnAfterClaimDetailsNotified =
        ClaimDetailsNotifiedTransitionBuilder::getPredicateTakenOfflineSDONotDrawnAfterClaimDetailsNotified;

    private static boolean getPredicateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(CaseData caseData) {
        boolean baseCondition = caseData.getReasonNotSuitableSDO() != null
            && StringUtils.isNotBlank(caseData.getReasonNotSuitableSDO().getInput())
            && caseData.getTakenOfflineDate() != null
            && caseData.getRespondent1AcknowledgeNotificationDate() == null
            && caseData.getRespondent1ResponseDate() == null
            && caseData.getRespondent1TimeExtensionDate() == null
            && caseData.getClaimDismissedDate() == null;

        return switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP, ONE_V_TWO_ONE_LEGAL_REP -> baseCondition
                && caseData.getRespondent2ResponseDate() == null
                && caseData.getRespondent2AcknowledgeNotificationDate() == null
                && caseData.getRespondent2TimeExtensionDate() == null;
            default -> baseCondition;
        };
    }
}
