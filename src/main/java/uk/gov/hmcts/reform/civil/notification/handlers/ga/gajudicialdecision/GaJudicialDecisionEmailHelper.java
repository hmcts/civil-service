package uk.gov.hmcts.reform.civil.notification.handlers.ga.gajudicialdecision;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.handler.callback.user.JudicialFinalDecisionHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.utils.NotificationCriterion;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_TITLE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_JUDICIAL_CONCURRENT_DATE_TEXT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_JUDICIAL_SEQUENTIAL_DATE_TEXT_RESPONDENT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_LIP_APPLICANT_NAME;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_LIP_RESP_NAME;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_REQUEST_FOR_INFORMATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GENAPP_REFERENCE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.PARTY_REFERENCE;
import static uk.gov.hmcts.reform.civil.ga.service.JudicialNotificationService.useDamageTemplate;
import static uk.gov.hmcts.reform.civil.ga.service.JudicialNotificationService.useOcmcTemplate;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.addAllFooterItems;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.areRespondentSolicitorsPresent;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isGeneralAppConsentOrder;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.notificationCriterion;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.requiredGAType;
import static uk.gov.hmcts.reform.civil.ga.utils.NotificationCriterion.NON_CRITERION;
import static uk.gov.hmcts.reform.civil.ga.utils.NotificationCriterion.REQUEST_FOR_INFORMATION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;

@Component
@RequiredArgsConstructor
public class GaJudicialDecisionEmailHelper {

    public static final String REFERENCE_TEMPLATE = "general-apps-judicial-notification-make-decision-%s";

    private final NotificationsProperties templates;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private final GaForLipService gaForLipService;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;

    public boolean shouldNotify(GeneralApplicationCaseData data, boolean applicant) {
        if (notificationCriterion(data) == NON_CRITERION) {
            return false;
        }
        if (notificationCriterion(data) == REQUEST_FOR_INFORMATION
            && data.getCcdState() != APPLICATION_ADD_PAYMENT && applicantOnly(data)) {
            return applicant;
        }
        return applicant || areRespondentSolicitorsPresent(data);
    }

    public void prepareCase(GeneralApplicationCaseData data) {
        // The old respondent callback returned this deadline to CCD, even when no respondent email was sent.
        if (notificationCriterion(data) == REQUEST_FOR_INFORMATION
            && data.getCcdState() == APPLICATION_ADD_PAYMENT
            && data.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() == SEND_APP_TO_OTHER_PARTY) {
            data.generalAppNotificationDeadlineDate(
                deadlinesCalculator.calculateApplicantResponseDeadline(time.now(), 5));
        }
    }

    public String template(GeneralApplicationCaseData data, boolean applicant) {
        NotificationCriterion criterion = notificationCriterion(data);
        if (criterion == NotificationCriterion.JUDGE_APPROVED_THE_ORDER && useDamageTemplate(data)) {
            return templates.getJudgeApproveOrderToStrikeOutDamages();
        }
        if (criterion == NotificationCriterion.JUDGE_APPROVED_THE_ORDER && useOcmcTemplate(data)) {
            return templates.getJudgeApproveOrderToStrikeOutOCMC();
        }
        if (applicant ? gaForLipService.isLipApp(data) : gaForLipService.isLipResp(data)) {
            return applicant ? applicantLipTemplate(data) : respondentLipTemplate(data);
        }
        return switch (criterion) {
            case CONCURRENT_WRITTEN_REP -> applicant
                ? templates.getWrittenRepConcurrentRepresentationApplicantEmailTemplate()
                : templates.getWrittenRepConcurrentRepresentationRespondentEmailTemplate();
            case SEQUENTIAL_WRITTEN_REP -> applicant
                ? templates.getWrittenRepSequentialRepresentationApplicantEmailTemplate()
                : templates.getWrittenRepSequentialRepresentationRespondentEmailTemplate();
            case LIST_FOR_HEARING -> applicant ? templates.getJudgeListsForHearingApplicantEmailTemplate()
                : templates.getJudgeListsForHearingRespondentEmailTemplate();
            case JUDGE_FREE_FORM_ORDER -> applicant ? templates.getJudgeFreeFormOrderApplicantEmailTemplate()
                : templates.getJudgeFreeFormOrderRespondentEmailTemplate();
            case JUDGE_APPROVED_THE_ORDER -> applicant ? templates.getJudgeForApprovedCaseApplicantEmailTemplate()
                : templates.getJudgeForApproveRespondentEmailTemplate();
            case JUDGE_DISMISSED_APPLICATION -> applicant ? templates.getJudgeDismissesOrderApplicantEmailTemplate()
                : templates.getJudgeDismissesOrderRespondentEmailTemplate();
            case JUDGE_DIRECTION_ORDER -> applicant ? templates.getJudgeForDirectionOrderApplicantEmailTemplate()
                : templates.getJudgeForDirectionOrderRespondentEmailTemplate();
            case REQUEST_FOR_INFORMATION -> requestInformationTemplate(data, applicant);
            default -> throw new IllegalStateException("No judicial decision notification criterion");
        };
    }

    public Map<String, String> properties(GaNotificationContext context, boolean applicant) {
        GeneralApplicationCaseData data = context.gaCaseData();
        Map<String, String> properties = new HashMap<>();
        properties.put(GENAPP_REFERENCE, Objects.requireNonNull(data.getCcdCaseReference()).toString());
        properties.put(PARTY_REFERENCE, Objects.requireNonNullElse(data.getEmailPartyReference(),
            "Claimant Reference: Not provided - Defendant Reference: Not provided"));
        properties.put(CASE_REFERENCE, Objects.requireNonNull(data.getGeneralAppParentCaseLink().getCaseReference()));
        properties.put(GA_APPLICATION_TYPE, Objects.requireNonNull(requiredGAType(data)));
        addLipProperties(properties, data, applicant);
        addDecisionProperties(properties, data, applicant);
        addAllFooterItems(data, context.mainCaseData(), properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabledGa(data));
        return properties;
    }

    private void addLipProperties(Map<String, String> properties, GeneralApplicationCaseData data, boolean applicant) {
        if (!gaForLipService.isGaForLip(data)) {
            return;
        }
        if (gaForLipService.isLipApp(data)) {
            properties.put(GA_LIP_APPLICANT_NAME, Objects.requireNonNull(data.getApplicantPartyName()));
        }
        // This used to depend on the respondent task id, which the single NOTIFY_EVENT replaces.
        boolean respondentDecision = !applicant && ("MAKE_DECISION".equals(data.getBusinessProcess().getCamundaEvent())
            || "UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION".equals(data.getBusinessProcess().getCamundaEvent()));
        if (gaForLipService.isLipResp(data) && (data.getCcdState() == APPLICATION_ADD_PAYMENT || respondentDecision)) {
            properties.put(GA_LIP_RESP_NAME, Objects.requireNonNull(data.getParentClaimantIsApplicant() == NO
                ? data.getClaimant1PartyName() : data.getDefendant1PartyName()));
            properties.remove(GA_LIP_APPLICANT_NAME);
        }
        properties.put(CASE_TITLE, Objects.requireNonNull(JudicialFinalDecisionHandler.getAllPartyNames(data)));
    }

    private void addDecisionProperties(Map<String, String> properties, GeneralApplicationCaseData data, boolean applicant) {
        switch (notificationCriterion(data)) {
            case CONCURRENT_WRITTEN_REP -> properties.put(GA_JUDICIAL_CONCURRENT_DATE_TEXT,
                formatDate(data.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                    .getWrittenConcurrentRepresentationsBy()));
            case SEQUENTIAL_WRITTEN_REP -> properties.put(GA_JUDICIAL_SEQUENTIAL_DATE_TEXT_RESPONDENT,
                formatDate(data.getJudicialDecisionMakeAnOrderForWrittenRepresentations()
                    .getSequentialApplicantMustRespondWithin()));
            case REQUEST_FOR_INFORMATION -> {
                if (!applicant && data.getCcdState() == APPLICATION_ADD_PAYMENT) {
                    properties.put(GA_NOTIFICATION_DEADLINE, data.getGeneralAppNotificationDeadlineDate() == null ? null
                        : DateFormatHelper.formatLocalDateTime(data.getGeneralAppNotificationDeadlineDate(), DATE));
                } else if (!applicantOnly(data)) {
                    properties.put(GA_REQUEST_FOR_INFORMATION_DEADLINE,
                        data.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoByDate() == null ? null
                            : DateFormatHelper.formatLocalDate(
                                data.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoByDate(), DATE));
                }
            }
            default -> {
                // Other decisions use only the common properties and have no additional deadline fields.
            }
        }
    }

    private String formatDate(LocalDate date) {
        return date == null ? null : DateFormatHelper.formatLocalDate(date, DATE);
    }

    private String requestInformationTemplate(GeneralApplicationCaseData data, boolean applicant) {
        if (!applicant && data.getCcdState() == APPLICATION_ADD_PAYMENT) {
            return templates.getGeneralApplicationRespondentEmailTemplate();
        }
        if (applicantOnly(data)) {
            return templates.getJudgeUncloakApplicationEmailTemplate();
        }
        return applicant ? templates.getJudgeRequestForInformationApplicantEmailTemplate()
            : templates.getJudgeRequestForInformationRespondentEmailTemplate();
    }

    private boolean applicantOnly(GeneralApplicationCaseData data) {
        boolean withoutNotice = data.getGeneralAppRespondentAgreement().getHasAgreed() == NO
            && data.getGeneralAppInformOtherParty() != null
            && data.getGeneralAppInformOtherParty().getIsWithNotice() == NO
            && data.getGeneralAppPBADetails().getAdditionalPaymentDetails() == null;
        boolean consent = isGeneralAppConsentOrder(data)
            && data.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() == SEND_APP_TO_OTHER_PARTY
            && data.getGeneralAppPBADetails().getAdditionalPaymentDetails() == null;
        return (withoutNotice || consent)
            && data.getJudicialDecisionRequestMoreInfo().getRequestMoreInfoOption() == SEND_APP_TO_OTHER_PARTY;
    }

    private String applicantLipTemplate(GeneralApplicationCaseData data) {
        return data.isApplicantBilingual() ? templates.getLipGeneralAppApplicantEmailTemplateInWelsh()
            : templates.getLipGeneralAppApplicantEmailTemplate();
    }

    private String respondentLipTemplate(GeneralApplicationCaseData data) {
        return data.isRespondentBilingual() ? templates.getLipGeneralAppRespondentEmailTemplateInWelsh()
            : templates.getLipGeneralAppRespondentEmailTemplate();
    }
}
