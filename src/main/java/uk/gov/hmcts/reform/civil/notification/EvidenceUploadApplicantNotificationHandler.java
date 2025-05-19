package uk.gov.hmcts.reform.civil.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addLipContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Service
@RequiredArgsConstructor
public class EvidenceUploadApplicantNotificationHandler implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "evidence-upload-notification-%s";
    private final NotificationService notificationService;
    private final OrganisationService organisationService;
    private final NotificationsProperties notificationsProperties;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;

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
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, YesOrNo.NO.equals(caseData.getApplicant1Represented())
                ? caseData.getLegacyCaseReference()
                : caseData.getCcdCaseReference().toString(),
            UPLOADED_DOCUMENTS, caseData.getNotificationText(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_LEGAL_ORG_NAME_SPEC, YesOrNo.NO.equals(caseData.getApplicant1Represented())
            ? caseData.getApplicant1().getPartyName()
                : getApplicantLegalOrganizationName(caseData, organisationService),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        if (isApplicantLip(caseData)) {
            addLipContact(caseData, properties, featureToggleService.isQueryManagementLRsEnabled(),
                          featureToggleService.isLipQueryManagementEnabled(caseData));
        } else {
            addSpecAndUnspecContact(caseData, properties, configuration,
                                    featureToggleService.isQueryManagementLRsEnabled());
        }
        return properties;
    }
}
