package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.offline.otherresponses;

import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_RESPONSE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class DefRespCaseOfflineHelper {

    private DefRespCaseOfflineHelper() {
        //NO-OP
    }

    public static Map<String, String> caseOfflineNotificationProperties(CaseData caseData) {
        if (ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
            return Map.of(
                REASON, getReasonToBeDisplayed(caseData.getRespondent1ClaimResponseType())
            );
        }

        if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            String responseTypeToApplicant2 = String.valueOf(caseData.getRespondent1ClaimResponseTypeToApplicant2());
            return Map.of(
                REASON, getReasonToBeDisplayed(caseData.getRespondent1ClaimResponseType())
                    .concat(" against " + caseData.getApplicant1().getPartyName())
                    .concat(" and " + responseTypeToApplicant2)
                    .concat(" against " + caseData.getApplicant2().getPartyName())
            );
        }

        //1v2 template is used and expects different data
        return Map.of(
            RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
            RESPONDENT_ONE_RESPONSE, getReasonToBeDisplayed(caseData.getRespondent1ClaimResponseType()),
            RESPONDENT_TWO_RESPONSE, getReasonToBeDisplayed(caseData.getRespondent2ClaimResponseType())
        );
    }

    private static String getReasonToBeDisplayed(RespondentResponseType responseType) {
        return (responseType != null)
            ? (responseType.getDisplayedValue() != null ? responseType.getDisplayedValue() : "")
            : "";
    }
}
