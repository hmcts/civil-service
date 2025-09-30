package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.handler.callback.user.JudicialFinalDecisionHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.APPLICANT_REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.CASE_TITLE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.GA_LIP_RESP_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.GA_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.GENAPP_REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.PARTY_REFERENCE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil.isNotificationCriteriaSatisfied;
import static uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil.isUrgentApplnNotificationCriteriaSatisfied;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationCreationNotificationService  implements NotificationDataGA {

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

    public  CaseData sendNotification(CaseData caseData) throws NotificationException {

        var caseReference = caseData.getCcdCaseReference();
        log.info("Initiating notification process for Case ID: {}", caseReference);
        CaseData civilCaseData = caseDetailsConverter
            .toCaseDataGA(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        CaseData updatedCaseData = solicitorEmailValidation.validateSolicitorEmail(civilCaseData, caseData);

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

    public boolean isFeePaid(CaseData caseData) {
        return caseData.getGeneralAppPBADetails() != null
            && (caseData.getGeneralAppPBADetails().getFee().getCode().equals("FREE")
            || (caseData.getGeneralAppPBADetails().getPaymentDetails() != null
            && caseData.getGeneralAppPBADetails().getPaymentDetails().getStatus().equals(PaymentStatus.SUCCESS)));
    }

    private String getTemplate(CaseData caseData, boolean urgent, CaseData civilCaseData) {
        if (gaForLipService.isLipResp(caseData)) {
            return getLiPTemplate(civilCaseData, caseData);
        } else {
            return urgent ? notificationProperties
                    .getUrgentGeneralAppRespondentEmailTemplate() : notificationProperties
                    .getGeneralApplicationRespondentEmailTemplate();
        }
    }

    private String getLiPTemplate(CaseData civilCaseData, CaseData caseData) {
        return caseData.isRespondentBilingual()
            ? notificationProperties.getLipGeneralAppRespondentEmailTemplateInWelsh()
            : notificationProperties.getLipGeneralAppRespondentEmailTemplate();
    }

    private String getSolicitorReferences(String emailPartyReference) {
        return Objects.requireNonNullElse(emailPartyReference, EMPTY_SOLICITOR_REFERENCES_1V1);
    }

    private void sendNotificationToGeneralAppRespondent(CaseData caseData, CaseData mainCaseData, String recipient, String emailTemplate)
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
    public Map<String, String> addProperties(CaseData caseData, CaseData mainCaseData) {
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
                           featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

}
