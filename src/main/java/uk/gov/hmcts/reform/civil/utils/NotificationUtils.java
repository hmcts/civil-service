package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_RESPONSE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.utils.CaseCategoryUtils.isSpecCaseCategory;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class NotificationUtils {

    private NotificationUtils() {
        //NO-OP
    }

    public static Boolean isRespondent1(CallbackParams callbackParams, CaseEvent matchEvent) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return caseEvent.equals(matchEvent);
    }

    public static boolean is1v1Or2v1Case(CaseData caseData) {
        return getMultiPartyScenario(caseData).equals(ONE_V_ONE)
            || getMultiPartyScenario(caseData).equals(TWO_V_ONE);
    }

    public static Map<String, String> caseOfflineNotificationAddProperties(
        CaseData caseData, boolean accessProfilesEnabled) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
                PARTY_REFERENCES, buildPartiesReferences(caseData)
            );
        } else if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            String responseTypeToApplicant2 = isSpecCaseCategory(caseData, accessProfilesEnabled)
                ? caseData.getClaimant1ClaimResponseTypeForSpec().getDisplayedValue()
                : caseData.getRespondent1ClaimResponseTypeToApplicant2().toString();
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                REASON, isSpecCaseCategory(caseData, accessProfilesEnabled)
                    ? caseData.getClaimant1ClaimResponseTypeForSpec().getDisplayedValue()
                    : caseData.getRespondent1ClaimResponseType().getDisplayedValue()
                    .concat(" against " + caseData.getApplicant1().getPartyName())
                    .concat(" and " + responseTypeToApplicant2)
                    .concat(" against " + caseData.getApplicant2().getPartyName()),
                PARTY_REFERENCES, buildPartiesReferences(caseData)
            );
        } else {
            //1v2 template is used and expects different data
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
                RESPONDENT_ONE_RESPONSE, isSpecCaseCategory(caseData, accessProfilesEnabled)
                    ? caseData.getRespondent1ClaimResponseTypeForSpec().getDisplayedValue()
                    : caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
                RESPONDENT_TWO_RESPONSE, isSpecCaseCategory(caseData, accessProfilesEnabled)
                    ? caseData.getRespondent2ClaimResponseTypeForSpec().getDisplayedValue()
                    : caseData.getRespondent2ClaimResponseType().getDisplayedValue(),
                PARTY_REFERENCES, buildPartiesReferences(caseData)
            );
        }
    }

    public static String getSolicitorClaimDismissedProperty(List<String> stateHistoryNameList,
                                                            NotificationsProperties notificationsProperties) {
        //scenerio 1: Claim notification does not happen within 4 months of issue
        if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName())) {
            return notificationsProperties.getSolicitorClaimDismissedWithin4Months();
        } else if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName())) {
            //scenerio 2: Claims details notification is not completed within 14 days of the claim notification step
            return notificationsProperties.getSolicitorClaimDismissedWithin14Days();
        } else if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName())) {
            //scenerio 3 Claimant does not give their intention by the given deadline
            return notificationsProperties.getSolicitorClaimDismissedWithinDeadline();
        } else {
            return notificationsProperties.getSolicitorClaimDismissedWithinDeadline();
        }
    }
}
