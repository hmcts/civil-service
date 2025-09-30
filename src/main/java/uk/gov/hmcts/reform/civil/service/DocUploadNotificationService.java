package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.addAllFooterItems;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocUploadNotificationService implements NotificationDataGA {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationProperties;
    private static final String REFERENCE_TEMPLATE_DOC_UPLOAD = "general-apps-notice-of-document-upload-%s";
    private static final String EMPTY_SOLICITOR_REFERENCES_1V1 = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private final GaForLipService gaForLipService;
    private final Map<String, String> customProps = new HashMap<>();
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    public void notifyApplicantEvidenceUpload(CaseData caseData) throws NotificationException {
        log.info("Starting applicant evidence upload notification for Case ID: {}", caseData.getCcdCaseReference());
        String email = caseData.getGeneralAppApplnSolicitor().getEmail();
        CaseData civilCaseData = caseDetailsConverter.toCaseDataGA(coreCaseDataService
                .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));
        if (null != email) {
            notificationService.sendMail(
                    email,
                    gaForLipService.isLipApp(caseData) ? getLiPApplicantTemplate(caseData)
                            : notificationProperties.getEvidenceUploadTemplate(),
                    addProperties(caseData, civilCaseData),
                    String.format(
                            REFERENCE_TEMPLATE_DOC_UPLOAD,
                            caseData.getCcdCaseReference()
                    )
            );
            log.info("Applicant evidence upload notification sent to: {} for Case ID: {}", email, caseData.getCcdCaseReference());
        }
    }

    public void notifyRespondentEvidenceUpload(CaseData caseData) throws NotificationException {

        log.info("Starting respondent evidence upload notification for Case ID: {}", caseData.getCcdCaseReference());
        CaseData civilCaseData = caseDetailsConverter.toCaseDataGA(coreCaseDataService
                .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        caseData.getGeneralAppRespondentSolicitors().forEach(
                respondentSolicitor -> {
                    notificationService.sendMail(
                            respondentSolicitor.getValue().getEmail(),
                            gaForLipService.isLipResp(caseData)
                                    ? getLiPRespondentTemplate(caseData)
                                    : notificationProperties.getEvidenceUploadTemplate(),
                            addProperties(caseData, civilCaseData),
                            String.format(
                                    REFERENCE_TEMPLATE_DOC_UPLOAD,
                                    caseData.getCcdCaseReference()
                            )
                    );
                });
    }

    private String getLiPRespondentTemplate(CaseData caseData) {
        return caseData.isRespondentBilingual()
                ? notificationProperties.getLipGeneralAppRespondentEmailTemplateInWelsh()
                : notificationProperties.getLipGeneralAppRespondentEmailTemplate();
    }

    private String getLiPApplicantTemplate(CaseData caseData) {
        return caseData.isApplicantBilingual()
                ? notificationProperties.getLipGeneralAppApplicantEmailTemplateInWelsh()
                : notificationProperties.getLipGeneralAppApplicantEmailTemplate();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData, CaseData mainCaseData) {

        if (gaForLipService.isGaForLip(caseData)) {
            String caseTitle = getAllPartyNames(caseData);
            customProps.put(
                    CASE_TITLE,
                    Objects.requireNonNull(caseTitle)
            );
        }
        if (gaForLipService.isLipApp(caseData)) {
            String isLipAppName = caseData.getApplicantPartyName();

            customProps.put(GA_LIP_APPLICANT_NAME, Objects.requireNonNull(isLipAppName));

        } else if (gaForLipService.isLipResp(caseData)) {

            String isLipRespondentName = caseData.getDefendant1PartyName();
            customProps.put(
                    GA_LIP_RESP_NAME,
                    Objects.requireNonNull(isLipRespondentName)
            );
        } else {
            customProps.remove(GA_LIP_APPLICANT_NAME);
            customProps.remove(GA_LIP_RESP_NAME);
            customProps.remove(CASE_TITLE);
        }

        customProps.put(CASE_REFERENCE, caseData.getCcdCaseReference().toString());
        customProps.put(PARTY_REFERENCE,
                Objects.requireNonNull(getSolicitorReferences(caseData.getEmailPartyReference())));
        customProps.put(GENAPP_REFERENCE, String.valueOf(Objects.requireNonNull(caseData.getCcdCaseReference())));
        addAllFooterItems(caseData, mainCaseData, customProps, configuration,
                featureToggleService.isPublicQueryManagementEnabled(caseData));
        return customProps;
    }

    public static String getAllPartyNames(CaseData caseData) {
        return format("%s v %s%s",
                caseData.getClaimant1PartyName(),
                caseData.getDefendant1PartyName(),
                nonNull(caseData.getDefendant2PartyName())
                        && (NO.equals(caseData.getRespondent2SameLegalRepresentative())
                        || Objects.isNull(caseData.getRespondent2SameLegalRepresentative()))
                        ? ", " + caseData.getDefendant2PartyName() : "");
    }

    private String getSolicitorReferences(String emailPartyReference) {
        if (emailPartyReference != null) {
            return emailPartyReference;
        } else {
            return EMPTY_SOLICITOR_REFERENCES_1V1;
        }
    }

    public String getSurname(CaseData caseData) {
        String surname = "";

        if (caseData.getGeneralAppRespondentSolicitors().get(0).getValue().getSurname().isPresent()) {
            surname = caseData.getGeneralAppRespondentSolicitors().get(0).getValue().getSurname().orElse("");
        }
        return surname;
    }
}
