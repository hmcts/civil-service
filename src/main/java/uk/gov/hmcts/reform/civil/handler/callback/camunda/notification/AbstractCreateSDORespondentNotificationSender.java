package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

/**
 * When an SDO is created it is notified to applicants and defendants.
 * This class holds common code for the actual sending of the email.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractCreateSDORespondentNotificationSender implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    protected abstract String getDocReference(CaseData caseData);

    protected abstract String getRecipientEmail(CaseData caseData);

    void notifyRespondentPartySDOTriggered(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        String email = getRecipientEmail(caseData);
        if (StringUtils.isNotBlank(email)) {
            notificationService.sendMail(
                email,
                getSDOTemplate(callbackParams),
                isLipCase(caseEvent, caseData) ? addPropertiesLip(caseData) : addProperties(caseData),
                getDocReference(caseData)
            );
        } else {
            log.info("Party " + getRespondentLegalName(caseData)
                         + " has no email address for claim "
                         + caseData.getLegacyCaseReference());
        }
    }

    private String getSDOTemplate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        if (isLipCase(caseEvent, caseData)) {
            return caseData.isRespondentResponseBilingual()
                ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        }

        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            if (caseData.isRespondentResponseBilingual()) {
                return notificationsProperties.getSdoOrderedSpecBilingual();
            }

            return featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())
                ? notificationsProperties.getSdoOrderedSpecEa() : notificationsProperties.getSdoOrderedSpec();
        }
        return notificationsProperties.getSdoOrdered();

    }

    private boolean isLipCase(CaseEvent caseEvent, CaseData caseData) {
        return (CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED.equals(caseEvent) && caseData.isRespondent1LiP())
            || (CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED.equals(caseEvent) && caseData.isRespondent2LiP());
    }

    /**
     * Depending on the defendant being represented or otherwise, the legal name is different.
     *
     * @param caseData case data
     * @return email to use for the defendant
     */
    protected abstract String getRespondentLegalName(CaseData caseData);

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getRespondentLegalName(caseData)
            );
        }
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalName(caseData),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
        );
    }

    public Map<String, String> addPropertiesLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        );
    }
}
