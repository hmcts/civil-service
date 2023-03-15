package uk.gov.hmcts.reform.civil.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvidenceUploadApplicantNotificationHandler implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "evidence-upload-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public void notifyApplicantEvidenceUpload(CaseData caseData) throws NotificationException {
        //Send email to Applicant
        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getEvidenceUploadTemplate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
    }
}
