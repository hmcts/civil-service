package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.handler.callback.user.JudicialFinalDecisionHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.addAllFooterItems;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingScheduledNotificationService implements NotificationDataGA {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationsProperties notificationProperties;
    private final SolicitorEmailValidation solicitorEmailValidation;
    private final CoreCaseDataService coreCaseDataService;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;
    private final Map<String, String> customProps = new HashMap<>();
    private final GaForLipService gaForLipService;
    private static final String REFERENCE_TEMPLATE_HEARING = "general-apps-notice-of-hearing-%s";
    private static final String EMPTY_SOLICITOR_REFERENCES_1V1 = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private static final String RESPONDENT = "respondent";
    private static final String APPLICANT = "applicant";

    @Override
    public Map<String, String> addProperties(CaseData caseData, CaseData mainCaseData) {
        String hourMinute = caseData.getGaHearingNoticeDetail().getHearingTimeHourMinute();
        int hours = Integer.parseInt(hourMinute.substring(0, 2));
        int minutes = Integer.parseInt(hourMinute.substring(2, 4));
        LocalTime hearingTime = LocalTime.of(hours, minutes, 0);

        customProps.put(CASE_REFERENCE, caseData.getGeneralAppParentCaseLink().getCaseReference());
        customProps.put(GENAPP_REFERENCE, String.valueOf(Objects.requireNonNull(caseData.getCcdCaseReference())));
        customProps.put(GA_HEARING_DATE, DateFormatHelper
            .formatLocalDate(caseData.getGaHearingNoticeDetail().getHearingDate(), DATE));
        customProps.put(GA_HEARING_TIME, hearingTime.toString());
        customProps.put(PARTY_REFERENCE,
            Objects.requireNonNull(getSolicitorReferences(caseData.getEmailPartyReference())));
        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.toGaCaseData(caseData, objectMapper);
        if (gaCaseData != null && gaForLipService.isGaForLip(gaCaseData)) {
            customProps.put(CASE_TITLE, Objects.requireNonNull(JudicialFinalDecisionHandler
                                                                   .getAllPartyNames(gaCaseData)));
        } else {
            customProps.remove(CASE_TITLE);
            customProps.remove(GA_LIP_APPLICANT_NAME);
            customProps.remove(GA_LIP_RESP_NAME);
        }
        if (gaCaseData != null) {
            addAllFooterItems(gaCaseData, mainCaseData, customProps, configuration,
                               featureToggleService.isPublicQueryManagementEnabled(caseData));
        } else {
            addAllFooterItems(caseData, mainCaseData, customProps, configuration,
                               featureToggleService.isPublicQueryManagementEnabled(caseData));
        }
        return customProps;
    }

    public Map<String, String> addPropertiesByType(CaseData caseData, CaseData mainCaseData, String gaLipType) {
        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.toGaCaseData(caseData, objectMapper);
        if (gaCaseData != null && gaForLipService.isLipAppGa(gaCaseData) && gaLipType.equals(APPLICANT)) {
            String isLipAppName = caseData.getApplicantPartyName();
            customProps.put(
                GA_LIP_APPLICANT_NAME,
                Objects.requireNonNull(isLipAppName)
            );
            customProps.remove(GA_LIP_RESP_NAME);
        }

        if (gaCaseData != null && gaForLipService.isLipRespGa(gaCaseData) && gaLipType.equals(RESPONDENT)) {
            String isLipRespondentName = caseData.getDefendant1PartyName();
            customProps.remove(GA_LIP_APPLICANT_NAME);
            customProps.put(GA_LIP_RESP_NAME, Objects.requireNonNull(isLipRespondentName));
        }

        addProperties(caseData, mainCaseData);
        return customProps;
    }

    private void sendNotification(CaseData caseData, CaseData mainCaseData, String recipient, String template, String gaLipType) throws NotificationException {
        try {
            notificationService.sendMail(recipient,  template,
                                         addPropertiesByType(caseData, mainCaseData, gaLipType),
                                         String.format(REFERENCE_TEMPLATE_HEARING,
                                                       caseData.getGeneralAppParentCaseLink().getCaseReference()));
        } catch (NotificationException e) {
            throw new NotificationException(e);
        }
    }

    public CaseData sendNotificationForClaimant(CaseData caseData) throws NotificationException {

        CaseData civilCaseData = caseDetailsConverter
            .toCaseDataGA(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        GeneralApplicationCaseData gaCaseData = solicitorEmailValidation.validateSolicitorEmail(
            civilCaseData,
            GaCallbackDataUtil.toGaCaseData(caseData, objectMapper)
        );
        CaseData updatedCaseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, caseData, objectMapper);

        sendNotification(updatedCaseData, civilCaseData, updatedCaseData.getGeneralAppApplnSolicitor().getEmail(),
                         gaForLipService.isLipAppGa(gaCaseData)
                             ? getLiPApplicantTemplates(updatedCaseData)
                             : notificationProperties.getHearingNoticeTemplate(), APPLICANT);
        log.info("Sending hearing scheduled notification for claimant for Case ID: {}", updatedCaseData.getCcdCaseReference());

        return updatedCaseData;
    }

    private String getLiPApplicantTemplates(CaseData caseData) {
        return caseData.isApplicantBilingual()
            ? notificationProperties.getLipGeneralAppApplicantEmailTemplateInWelsh()
            : notificationProperties.getLipGeneralAppApplicantEmailTemplate();
    }

    private String getSolicitorReferences(String emailPartyReference) {
        if (emailPartyReference != null) {
            return emailPartyReference;
        } else {
            return EMPTY_SOLICITOR_REFERENCES_1V1;
        }
    }

    public CaseData sendNotificationForDefendant(CaseData caseData) throws NotificationException {

        CaseData civilCaseData = caseDetailsConverter
            .toCaseDataGA(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        GeneralApplicationCaseData gaCaseData = solicitorEmailValidation.validateSolicitorEmail(
            civilCaseData,
            GaCallbackDataUtil.toGaCaseData(caseData, objectMapper)
        );
        CaseData updatedCaseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, caseData, objectMapper);

        List<Element<GASolicitorDetailsGAspec>> respondentSolicitor = updatedCaseData
            .getGeneralAppRespondentSolicitors();
        CaseData finalCaseData = updatedCaseData;
        respondentSolicitor.forEach((respondent) -> sendNotification(
            finalCaseData,
            civilCaseData,
            respondent.getValue().getEmail(), gaForLipService.isLipRespGa(gaCaseData)
                ? getLiPRespondentTemplate(civilCaseData, finalCaseData)
                : notificationProperties.getHearingNoticeTemplate(), RESPONDENT));

        log.info("Sending hearing scheduled notification for respondent for Case ID: {}", updatedCaseData.getCcdCaseReference());
        return updatedCaseData;
    }

    private String getLiPRespondentTemplate(CaseData civilCaseData, CaseData caseData) {
        return caseData.isRespondentBilingual()
            ? notificationProperties.getLipGeneralAppRespondentEmailTemplateInWelsh()
            : notificationProperties.getLipGeneralAppRespondentEmailTemplate();
    }
}
