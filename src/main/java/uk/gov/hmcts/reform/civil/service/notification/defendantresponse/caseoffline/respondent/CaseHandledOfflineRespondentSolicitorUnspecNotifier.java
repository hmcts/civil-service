package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.CaseHandledOfflineRecipient;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

@Component
@AllArgsConstructor
public class CaseHandledOfflineRespondentSolicitorUnspecNotifier extends CaseHandledOfflineRespondentSolicitorNotifier {

    private static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-respondent-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    public void notifyRespondentSolicitorForCaseHandedOffline(CaseData caseData, CaseHandledOfflineRecipient recipientType) {
        sendNotificationToSolicitor(caseData, getRecipientEmailAddress(caseData, recipientType), getTemplateId(caseData));

    }

    private String getTemplateId(CaseData caseData) {
        if (is1v1Or2v1Case(caseData)) {
            return notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
        } else {

            return notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
        }
    }

    private void sendNotificationToSolicitor(CaseData caseData, String recipientEmailAddress, String templateID) {
        notificationService.sendMail(
            recipientEmailAddress,
            templateID,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return NotificationUtils.caseOfflineNotificationAddProperties(caseData);
    }

}
