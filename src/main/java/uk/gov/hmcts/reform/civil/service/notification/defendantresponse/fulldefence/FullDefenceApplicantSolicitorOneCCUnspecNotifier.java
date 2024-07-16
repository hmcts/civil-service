package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
@RequiredArgsConstructor
public class FullDefenceApplicantSolicitorOneCCUnspecNotifier extends FullDefenceSolicitorUnspecNotifier {

    //NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    public void notifySolicitorForDefendantResponse(CaseData caseData) {
        String recipient;
        recipient = getRecipient(caseData);
        sendNotificationToSolicitor(caseData, recipient);
    }

    @Override
    protected String getRecipient(CaseData caseData) {
        return caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        notificationService.sendMail(
            recipient,
            notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }
}
