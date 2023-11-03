package uk.gov.hmcts.reform.civil.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Service
@RequiredArgsConstructor
public class EvidenceUploadRespondentNotificationHandler implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "evidence-upload-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public void notifyRespondentEvidenceUpload(CaseData caseData, boolean isForRespondentSolicitor1) throws NotificationException {

        boolean isRespondentLip = false;

        String email = null;
        if (isForRespondentSolicitor1) {
            isRespondentLip = NO.equals(caseData.getRespondent1Represented());
            email = isRespondentLip ? caseData.getRespondent1().getPartyEmail()
                                        : caseData.getRespondentSolicitor1EmailAddress();
        } else if (caseData.getAddRespondent2() != null
                && caseData.getAddRespondent2().equals(YesOrNo.YES)
                && !NO.equals(caseData.getRespondent2Represented())
                && caseData.getRespondentSolicitor2EmailAddress() != null) {
            email = caseData.getRespondentSolicitor2EmailAddress();
        } else if (caseData.getAddRespondent2() != null
                && NO.equals(caseData.getRespondent2Represented())) {
            email = caseData.getRespondent2().getPartyEmail();
            isRespondentLip = true;
        }

        if (null != email && nonNull(caseData.getNotificationText()) && !caseData.getNotificationText().equals("NULLED")) {
            notificationService.sendMail(
                email,
                getTemplate(isRespondentLip),
                addProperties(caseData),
                String.format(
                    REFERENCE_TEMPLATE,
                    caseData.getLegacyCaseReference()
                )
            );
        }
    }

    public String getTemplate(boolean isRespondentLip) {
        return isRespondentLip ? notificationsProperties.getEvidenceUploadLipTemplate()
                                    : notificationsProperties.getEvidenceUploadTemplate();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            UPLOADED_DOCUMENTS, caseData.getNotificationText()
        );
    }
}
