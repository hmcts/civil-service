package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.ga.handler.callback.user.JudicialFinalDecisionHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isNotificationCriteriaSatisfied;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isUrgentApplnNotificationCriteriaSatisfied;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationCreationNotificationService implements NotificationDataGA {

    private static final String REFERENCE_TEMPLATE = "general-application-respondent-notification-%s";

    private static final String EMPTY_SOLICITOR_REFERENCES_1V1 = "Claimant Reference: Not provided - Defendant Reference: Not provided";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationProperties;

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final GaForLipService gaForLipService;

    private final SolicitorEmailValidation solicitorEmailValidation;
    private final NotificationsProperties notificationsProperties;

    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    public  GeneralApplicationCaseData sendNotification(GeneralApplicationCaseData caseData) throws NotificationException {

        var caseReference = caseData.getCcdCaseReference();
        log.info("Initiating notification process for Case ID: {}", caseReference);
        GeneralApplicationCaseData civilCaseData = caseDetailsConverter
            .toGeneralApplicationCaseData(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        GeneralApplicationCaseData updatedCaseData = solicitorEmailValidation.validateSolicitorEmail(civilCaseData, caseData);

        boolean isNotificationCriteriaSatisfied = isNotificationCriteriaSatisfied(updatedCaseData);

        /*
         * Send email to Respondents if application is withNotice and non-urgent
         * */
        if (isNotificationCriteriaSatisfied) {

            log.info("Sending general notification to respondents for Case ID: {}", caseReference);
            List<Element<GASolicitorDetailsGAspec>> respondentSolicitor = updatedCaseData
                .getGeneralAppRespondentSolicitors();

            respondentSolicitor
                .forEach((RS) ->
                             sendNotificationToGeneralAppRespondent(updatedCaseData,
                                                                    civilCaseData,
                                                                    RS.getValue().getEmail(),
                                     getTemplate(updatedCaseData, false, civilCaseData)
                             ));
        }

        /*
        * Send email to Respondent if application is urgent, with notice and fee is paid
        * */
        boolean isUrgentApplnNotificationCriteriaSatisfied
            = isUrgentApplnNotificationCriteriaSatisfied(updatedCaseData);

        if (isUrgentApplnNotificationCriteriaSatisfied
            && isFeePaid(updatedCaseData)) {
            log.info("Sending urgent notification to respondents for Case ID: {}", caseReference);

            List<Element<GASolicitorDetailsGAspec>> respondentSolicitor = updatedCaseData
                .getGeneralAppRespondentSolicitors();

            respondentSolicitor
                .forEach((RS) ->
                             sendNotificationToGeneralAppRespondent(
                                 updatedCaseData,
                                 civilCaseData,
                                 RS.getValue().getEmail(),
                                 getTemplate(updatedCaseData, true, civilCaseData)));
        }

        return caseData;
    }

    public boolean isFeePaid(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppPBADetails() != null
            && (caseData.getGeneralAppPBADetails().getFee().getCode().equals("FREE")
            || (caseData.getGeneralAppPBADetails().getPaymentDetails() != null
            && caseData.getGeneralAppPBADetails().getPaymentDetails().getStatus().equals(PaymentStatus.SUCCESS)));
    }

    private String getTemplate(GeneralApplicationCaseData caseData, boolean urgent, GeneralApplicationCaseData civilCaseData) {
        if (gaForLipService.isLipResp(caseData)) {
            return getLiPTemplate(civilCaseData, caseData);
        } else {
            return urgent ? notificationProperties
                    .getUrgentGeneralAppRespondentEmailTemplate() : notificationProperties
                    .getGeneralApplicationRespondentEmailTemplate();
        }
    }

    private String getLiPTemplate(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData) {
        return caseData.isRespondentBilingual()
            ? notificationProperties.getLipGeneralAppRespondentEmailTemplateInWelsh()
            : notificationProperties.getLipGeneralAppRespondentEmailTemplate();
    }

    private String getSolicitorReferences(String emailPartyReference) {
        if (emailPartyReference != null) {
            return emailPartyReference;
        } else {
            return EMPTY_SOLICITOR_REFERENCES_1V1;
        }
    }

    private void sendNotificationToGeneralAppRespondent(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData, String recipient, String emailTemplate)
        throws NotificationException {
        var caseReference = caseData.getCcdCaseReference();
        try {
            log.info("Sending notification to recipient for Case ID: {} with template: {}", caseReference, emailTemplate);
            notificationService.sendMail(
                recipient,
                emailTemplate,
                addProperties(caseData, mainCaseData),
                String.format(REFERENCE_TEMPLATE, caseData.getGeneralAppParentCaseLink().getCaseReference())
            );
            log.info("Notification sent successfully for Case ID: {}", caseReference);
        } catch (NotificationException e) {
            log.error("Failed to send notification for Case ID: {}", caseReference, e);
            throw new NotificationException(e);
        }
    }

    @Override
    public Map<String, String> addProperties(GeneralApplicationCaseData caseData, GeneralApplicationCaseData mainCaseData) {
        String lipRespName = "";
        String caseTitle = "";
        if (gaForLipService.isLipResp(caseData)) {
            lipRespName = caseData.getParentClaimantIsApplicant().equals(YES) ? caseData.getDefendant1PartyName() :
                caseData.getClaimant1PartyName();
            caseTitle = JudicialFinalDecisionHandler.getAllPartyNames(caseData);

        }
        HashMap<String, String> properties = new HashMap<>(Map.of(
            APPLICANT_REFERENCE, YES.equals(caseData.getParentClaimantIsApplicant()) ? "claimant" : "respondent",
            CASE_REFERENCE, caseData.getGeneralAppParentCaseLink().getCaseReference(),
            GENAPP_REFERENCE, String.valueOf(Objects.requireNonNull(caseData.getCcdCaseReference())),
            GA_NOTIFICATION_DEADLINE, DateFormatHelper
                .formatLocalDateTime(caseData
                                         .getGeneralAppNotificationDeadlineDate(), DATE),
            PARTY_REFERENCE,
            Objects.requireNonNull(getSolicitorReferences(caseData.getEmailPartyReference())),
            GA_LIP_RESP_NAME, lipRespName,

            CASE_TITLE, Objects.requireNonNull(caseTitle)
        ));
        addAllFooterItems(caseData, mainCaseData, properties, configuration,
                           featureToggleService.isPublicQueryManagementEnabledGa(caseData));
        return properties;
    }

}
