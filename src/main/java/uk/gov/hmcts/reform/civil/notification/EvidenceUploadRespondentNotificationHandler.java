package uk.gov.hmcts.reform.civil.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Service
@RequiredArgsConstructor
public class EvidenceUploadRespondentNotificationHandler implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "evidence-upload-notification-%s";
    private final NotificationService notificationService;
    private final OrganisationService organisationService;
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
            Map<String, String> properties = addProperties(caseData);
            properties.put(CLAIM_REFERENCE_NUMBER, getCaseRef(caseData, isRespondentLip));
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getOrgName(caseData, isForRespondentSolicitor1, isRespondentLip));
            notificationService.sendMail(
                email,
                getTemplate(caseData, isRespondentLip),
                properties,
                String.format(
                    REFERENCE_TEMPLATE,
                    caseData.getLegacyCaseReference()
                )
            );
        }
    }

    public String getTemplate(CaseData caseData, boolean isRespondentLip) {
        if (isRespondentLip && caseData.isRespondentResponseBilingual()) {
            return notificationsProperties.getEvidenceUploadLipTemplateWelsh();
        } else if (isRespondentLip) {
            return notificationsProperties.getEvidenceUploadLipTemplate();
        } else {
            return notificationsProperties.getEvidenceUploadTemplate();
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            UPLOADED_DOCUMENTS, caseData.getNotificationText(),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    private String getCaseRef(CaseData caseData, boolean isLip) {
        return isLip ? caseData.getLegacyCaseReference() : caseData.getCcdCaseReference().toString();
    }

    private String getOrgName(CaseData caseData, boolean isForRespondentSolicitor1, boolean isLip) {
        return isLip && isForRespondentSolicitor1 ? caseData.getRespondent1().getPartyName() : isForRespondentSolicitor1
            ? getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService)
            : getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService);
    }
}
