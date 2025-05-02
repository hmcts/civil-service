package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.counterclaimordivergedresponse;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_RESPONSE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@AllArgsConstructor
public class SpecCaseOfflineHelper {

    private final NotificationsProperties notificationsProperties;

    public String getClaimantTemplateForLipVLRSpecClaims(CaseData caseData) {
        return caseData.isClaimantBilingual()
                ? notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()
                : notificationsProperties.getClaimantLipClaimUpdatedTemplate();
    }

    public String getApplicantTemplateForSpecClaims(CaseData caseData) {
        if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && (caseData.getRespondent2() == null || YES.equals(caseData.getRespondentResponseIsSame()))) {
            return notificationsProperties.getClaimantSolicitorCounterClaimForSpec();
        } else {
            if (is1v1Or2v1Case(caseData)) {
                return notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
            } else {
                if (isOneVTwoTwoLegalRep(caseData)) {
                    return notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
                } else {
                    return notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
                }
            }
        }
    }

    public String getRespTemplateForSpecClaims(CaseData caseData) {
        if ((COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec()) &&
                (caseData.getRespondent2() == null || YES.equals(caseData.getRespondentResponseIsSame())))) {
            return notificationsProperties.getRespondentSolicitorCounterClaimForSpec();
        } else {
            return notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        }
    }

    public static Map<String, String> caseOfflineNotificationProperties(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            return Map.of(
                REASON, caseData.getRespondent1ClaimResponseTypeForSpec().getDisplayedValue()
            );
        } else if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            String responseTypeToApplicant2 = caseData.getClaimant1ClaimResponseTypeForSpec().getDisplayedValue();
            return Map.of(
                REASON, caseData.getClaimant1ClaimResponseTypeForSpec().getDisplayedValue()
                    .concat(" against " + caseData.getApplicant1().getPartyName())
                    .concat(" and " + responseTypeToApplicant2)
                    .concat(" against " + caseData.getApplicant2().getPartyName())
            );
        } else {
            //1v2 template is used and expects different data
            return Map.of(
                RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
                RESPONDENT_ONE_RESPONSE, caseData.getRespondent1ClaimResponseTypeForSpec().getDisplayedValue(),
                RESPONDENT_TWO_RESPONSE, caseData.getRespondent2ClaimResponseTypeForSpec().getDisplayedValue()
            );
        }
    }
}
