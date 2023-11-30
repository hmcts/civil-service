package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendGenericNotificationLipService implements NotificationData {

    private static final String REFERENCE_TEMPLATE =
        "generic-notification-lip-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public void sendGenericNotificationLip(CaseData caseData) {
        notificationService.sendMail(
            getRecipientEmail(caseData),
            getNotificationTemplate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        );
    }

    private String getNotificationTemplate() {
        return notificationsProperties.getNotifyLipUpdateTemplate();
    }

    private String getRecipientEmail(CaseData caseData) {
        return caseData.getClaimantUserDetails().getEmail();
    }
}
