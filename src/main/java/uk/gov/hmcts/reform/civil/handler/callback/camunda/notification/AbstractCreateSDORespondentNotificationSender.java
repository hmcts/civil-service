package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

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

    void notifyRespondentPartySDOTriggered(CaseData caseData) {
        String email = getRecipientEmail(caseData);
        if (StringUtils.isNotBlank(email)) {
            notificationService.sendMail(
                email,
                getSDOTemplate(caseData),
                addProperties(caseData),
                getDocReference(caseData)
            );
        } else {
            log.info("Party " + getRespondentLegalName(caseData)
                         + " has no email address for claim "
                         + caseData.getLegacyCaseReference());
        }
    }

    private String getSDOTemplate(CaseData caseData) {
        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            if (caseData.isRespondentResponseBilingual()) {
                return notificationsProperties.getSdoOrderedSpecBilingual();
            }
            return featureToggleService.isEarlyAdoptersEnabled()
                ? notificationsProperties.getSdoOrderedSpecEA() : notificationsProperties.getSdoOrderedSpec();
        }
        return featureToggleService.isEarlyAdoptersEnabled()
            ? notificationsProperties.getSdoOrderedEA() : notificationsProperties.getSdoOrdered();
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
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getRespondentLegalName(caseData)
            );
        }
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalName(caseData)
        );
    }
}
