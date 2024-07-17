package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
@RequiredArgsConstructor
public class FullDefenceRespondentSolicitorTwoCCUnspecNotifier extends FullDefenceSolicitorUnspecNotifier {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    protected String getRecipient(CaseData caseData) {
        return caseData.getRespondentSolicitor2EmailAddress();
    }

    protected void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

}
