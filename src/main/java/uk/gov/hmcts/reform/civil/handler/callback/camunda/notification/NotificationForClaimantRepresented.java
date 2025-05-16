package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_LIP_SOLICITOR;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationForClaimantRepresented extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;
    private final NotificationsSignatureConfiguration configuration;
    private final FeatureToggleService featureToggleService;
    private static final String REFERENCE_TEMPLATE_DEFENDANT =
        "notify-lip-after-noc-approval-%s";
    public static final String TASK_ID_APPLICANT = "NotifyClaimantLipAfterNocApproval";
    public static final String TASK_ID_RESPONDENT = "NotifyDefendantLipClaimantRepresented";
    public static final String TASK_ID_APPLICANT_SOLICITOR = "NotifyApplicantLipSolicitor";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Callback handler received illegal event: %s";

    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyLipAfterNocApproval
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        var event = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        switch (event) {
            case NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL:
                return TASK_ID_APPLICANT;
            case NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED:
                return TASK_ID_RESPONDENT;
            case NOTIFY_APPLICANT_LIP_SOLICITOR:
                return TASK_ID_APPLICANT_SOLICITOR;
            default:
                throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, event));
        }
    }

    private CallbackResponse notifyLipAfterNocApproval(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        String recipientEmail = getRecipientEmail(caseEvent, caseData);
        boolean isRespondentNotification = isRespondentNotification(caseEvent);
        boolean isApplicantSolicitorNotify = isApplicantSolicitorNotification(caseEvent);
        String templateId = getTemplateID(isRespondentNotification, isApplicantSolicitorNotify, caseData.isClaimantBilingual());
        boolean eligibleForNotification = isNotEmpty(recipientEmail) && templateId != null;
        if (eligibleForNotification) {
            notificationService.sendMail(
                recipientEmail,
                templateId,
                isApplicantSolicitorNotify ? addPropertiesApplicantSolicitor(caseData) : addProperties(caseData),
                String.format(REFERENCE_TEMPLATE_DEFENDANT, caseData.getLegacyCaseReference())
            );
        }

        if (!eligibleForNotification) {
            log.info("No recipientEmail or templateId provided, skipping notification for caseId {}", caseData.getCcdCaseReference());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(
            NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED,
            NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL,
            NOTIFY_APPLICANT_LIP_SOLICITOR
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    public Map<String, String> addPropertiesApplicantSolicitor(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIM_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
                LEGAL_ORG_APPLICANT1, getLegalOrganizationName(caseData.getApplicant1OrganisationPolicy()
                        .getOrganisation().getOrganisationID()),
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, configuration);
        addSpecAndUnspecContact(caseData, properties, configuration,
                                featureToggleService.isQueryManagementLRsEnabled());
        return properties;
    }

    private boolean isRespondentNotification(CaseEvent caseEvent) {
        return caseEvent.name()
            .equals(NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED.name());
    }

    private String getRecipientEmailForRespondent(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1())
            .map(Party::getPartyEmail)
            .orElse("");
    }

    private String getTemplateID(boolean isDefendantEvent, boolean isApplicantSolicitorNotify, boolean isBilingual) {
        if (isDefendantEvent) {
            return notificationsProperties.getNotifyRespondentLipForClaimantRepresentedTemplate();
        }
        if (isBilingual) {
            return notificationsProperties.getNotifyClaimantLipForNoLongerAccessWelshTemplate();
        }
        if (isApplicantSolicitorNotify) {
            return notificationsProperties.getNoticeOfChangeApplicantLipSolicitorTemplate();
        }
        return notificationsProperties.getNotifyClaimantLipForNoLongerAccessTemplate();
    }

    private String getRecipientEmail(CaseEvent event, CaseData caseData) {
        switch (event) {
            case NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL:
                return caseData.getApplicant1Email();
            case NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED:
                return getRecipientEmailForRespondent(caseData);
            case NOTIFY_APPLICANT_LIP_SOLICITOR:
                return getApplicantSolicitorEmail(caseData);
            default:
                throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, event));
        }
    }

    private boolean isApplicantSolicitorNotification(CaseEvent caseEvent) {
        return caseEvent.name()
                .equals(NOTIFY_APPLICANT_LIP_SOLICITOR.name());
    }

    private String getApplicantSolicitorEmail(CaseData caseData) {
        if (caseData.getApplicantSolicitor1UserDetails() != null) {
            return caseData.getApplicantSolicitor1UserDetails().getEmail();
        }
        return null;
    }

    public String getLegalOrganizationName(String id) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        return organisation.map(Organisation::getName).orElse(null);
    }
}
