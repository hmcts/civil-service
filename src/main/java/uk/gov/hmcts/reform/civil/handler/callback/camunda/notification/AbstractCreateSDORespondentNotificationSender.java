package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractCreateSDORespondentNotificationSender implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    protected abstract String getDocReference(CaseData caseData);

    protected abstract String getRecipientEmail(CaseData caseData);

    void notifyRespondentPartySDOTriggered(CaseData caseData) {
        String email = getRecipientEmail(caseData);
        if (StringUtils.isNotBlank(email)) {
            notificationService.sendMail(
                email,
                caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM
                    ? notificationsProperties.getSdoOrderedSpec()
                    : notificationsProperties.getSdoOrdered(),
                addProperties(caseData),
                getDocReference(caseData)
            );
        }
    }

    protected abstract String getRespondentLegalName(CaseData caseData);

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalName(caseData)
        );
    }
}
