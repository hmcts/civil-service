package uk.gov.hmcts.reform.civil.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class EvidenceUploadApplicantNotificationHandler implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "evidence-upload-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public void notifyApplicantEvidenceUpload(CaseData caseData) throws NotificationException {

        boolean isApplicantLip = isApplicantLip(caseData);

        //Send email to Applicant
        if (nonNull(caseData.getNotificationText()) && !caseData.getNotificationText().equals("NULLED")) {
            notificationService.sendMail(
                getEmail(caseData, isApplicantLip),
                getTemplate(caseData, isApplicantLip),
                addProperties(caseData),
                getReference(caseData)
            );
        }
    }

    private static String getReference(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    private String getTemplate(CaseData caseData, boolean isApplicantLip) {
        if (isApplicantLip && caseData.isClaimantBilingual()) {
            return notificationsProperties.getEvidenceUploadLipTemplateWelsh();
        } else if (isApplicantLip) {
            return notificationsProperties.getEvidenceUploadLipTemplate();
        } else {
            return notificationsProperties.getEvidenceUploadTemplate();
        }
    }

    private String getEmail(CaseData caseData, boolean isApplicantLip) {
        return isApplicantLip ? caseData.getApplicant1().getPartyEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    private boolean isApplicantLip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented());
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            UPLOADED_DOCUMENTS, caseData.getNotificationText()
        );
    }
}
