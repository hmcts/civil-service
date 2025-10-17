package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.addAllFooterItems;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocUploadNotificationService implements NotificationDataGA {

    private static final String REFERENCE_TEMPLATE_DOC_UPLOAD = "general-apps-notice-of-document-upload-%s";
    private static final String EMPTY_SOLICITOR_REFERENCES_1V1 =
        "Claimant Reference: Not provided - Defendant Reference: Not provided";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationProperties;
    private final GaForLipService gaForLipService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;
    private final ObjectMapper objectMapper;

    private final Map<String, String> customProps = new HashMap<>();

    public void notifyApplicantEvidenceUpload(CaseData caseData) throws NotificationException {
        notifyApplicantEvidenceUpload(caseData, toGaCaseData(caseData));
    }

    public void notifyApplicantEvidenceUpload(CaseData caseData,
                                              GeneralApplicationCaseData gaCaseData) throws NotificationException {
        log.info("Starting applicant evidence upload notification for Case ID: {}", caseData.getCcdCaseReference());
        String email = gaCaseData.getGeneralAppApplnSolicitor() != null
            ? gaCaseData.getGeneralAppApplnSolicitor().getEmail()
            : null;
        CaseData civilCaseData = getParentCaseData(gaCaseData);
        if (email != null) {
            notificationService.sendMail(
                email,
                gaForLipService.isLipAppGa(gaCaseData)
                    ? getLiPApplicantTemplate(gaCaseData)
                    : notificationProperties.getEvidenceUploadTemplate(),
                addProperties(gaCaseData, civilCaseData),
                format(REFERENCE_TEMPLATE_DOC_UPLOAD, caseData.getCcdCaseReference())
            );
            log.info(
                "Applicant evidence upload notification sent to: {} for Case ID: {}",
                email,
                caseData.getCcdCaseReference()
            );
        }
    }

    public void notifyRespondentEvidenceUpload(CaseData caseData) throws NotificationException {
        notifyRespondentEvidenceUpload(caseData, toGaCaseData(caseData));
    }

    public void notifyRespondentEvidenceUpload(CaseData caseData,
                                               GeneralApplicationCaseData gaCaseData) throws NotificationException {

        log.info("Starting respondent evidence upload notification for Case ID: {}", caseData.getCcdCaseReference());
        CaseData civilCaseData = getParentCaseData(gaCaseData);

        gaCaseData.getGeneralAppRespondentSolicitors().forEach(respondentSolicitor ->
            notificationService.sendMail(
                respondentSolicitor.getValue().getEmail(),
                gaForLipService.isLipRespGa(gaCaseData)
                    ? getLiPRespondentTemplate(gaCaseData)
                    : notificationProperties.getEvidenceUploadTemplate(),
                addProperties(gaCaseData, civilCaseData),
                format(REFERENCE_TEMPLATE_DOC_UPLOAD, caseData.getCcdCaseReference())
            )
        );
    }

    private String getLiPRespondentTemplate(GeneralApplicationCaseData caseData) {
        return caseData.isRespondentBilingual()
            ? notificationProperties.getLipGeneralAppRespondentEmailTemplateInWelsh()
            : notificationProperties.getLipGeneralAppRespondentEmailTemplate();
    }

    private String getLiPApplicantTemplate(GeneralApplicationCaseData caseData) {
        return caseData.isApplicantBilingual()
            ? notificationProperties.getLipGeneralAppApplicantEmailTemplateInWelsh()
            : notificationProperties.getLipGeneralAppApplicantEmailTemplate();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData, CaseData mainCaseData) {
        return addProperties(toGaCaseData(caseData), mainCaseData);
    }

    public Map<String, String> addProperties(GeneralApplicationCaseData caseData, CaseData mainCaseData) {

        if (gaForLipService.isGaForLip(caseData)) {
            String caseTitle = getAllPartyNames(caseData);
            customProps.put(CASE_TITLE, Objects.requireNonNull(caseTitle));
        } else {
            customProps.remove(CASE_TITLE);
        }

        if (gaForLipService.isLipAppGa(caseData)) {
            String isLipAppName = caseData.getApplicantPartyName();
            customProps.put(GA_LIP_APPLICANT_NAME, Objects.requireNonNull(isLipAppName));
        } else {
            customProps.remove(GA_LIP_APPLICANT_NAME);
        }

        if (gaForLipService.isLipRespGa(caseData)) {
            String isLipRespondentName = caseData.getDefendant1PartyName();
            customProps.put(GA_LIP_RESP_NAME, Objects.requireNonNull(isLipRespondentName));
        } else {
            customProps.remove(GA_LIP_RESP_NAME);
        }

        if (caseData.getCcdCaseReference() != null) {
            customProps.put(CASE_REFERENCE, caseData.getCcdCaseReference().toString());
            customProps.put(GENAPP_REFERENCE, caseData.getCcdCaseReference().toString());
        }

        customProps.put(
            PARTY_REFERENCE,
            Objects.requireNonNull(getSolicitorReferences(caseData.getEmailPartyReference()))
        );

        addAllFooterItems(
            caseData,
            mainCaseData,
            customProps,
            configuration,
            featureToggleService.isPublicQueryManagementEnabledGa(caseData)
        );
        return customProps;
    }

    public static String getAllPartyNames(GeneralApplicationCaseData caseData) {
        return format(
            "%s v %s%s",
            caseData.getClaimant1PartyName(),
            caseData.getDefendant1PartyName(),
            nonNull(caseData.getDefendant2PartyName())
                && (NO.equals(caseData.getRespondent2SameLegalRepresentative())
                || Objects.isNull(caseData.getRespondent2SameLegalRepresentative()))
                ? ", " + caseData.getDefendant2PartyName()
                : ""
        );
    }

    private String getSolicitorReferences(String emailPartyReference) {
        if (emailPartyReference != null) {
            return emailPartyReference;
        } else {
            return EMPTY_SOLICITOR_REFERENCES_1V1;
        }
    }

    public String getSurname(GeneralApplicationCaseData caseData) {
        if (!caseData.getGeneralAppRespondentSolicitors().isEmpty()
            && caseData.getGeneralAppRespondentSolicitors().get(0).getValue().getSurname().isPresent()) {
            return caseData.getGeneralAppRespondentSolicitors()
                .get(0)
                .getValue()
                .getSurname()
                .orElse("");
        }
        return "";
    }

    private CaseData getParentCaseData(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData.getGeneralAppParentCaseLink() == null
            || gaCaseData.getGeneralAppParentCaseLink().getCaseReference() == null) {
            return null;
        }
        return caseDetailsConverter.toCaseDataGA(
            coreCaseDataService.getCase(
                Long.parseLong(gaCaseData.getGeneralAppParentCaseLink().getCaseReference())
            )
        );
    }

    private GeneralApplicationCaseData toGaCaseData(CaseData caseData) {
        return GaCallbackDataUtil.toGaCaseData(caseData, objectMapper);
    }
}
