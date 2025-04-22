package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantDefendantAgreedMediationNotify;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSendMediationNotificationDefendant1LRCarm;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantDefendantAgreedMediationNotifier extends Notifier {

    private final FeatureToggleService featureToggleService;

    private static final String REFERENCE_TEMPLATE_APPLICANT = "mediation-agreement-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "mediation-agreement-respondent-notification-%s";

    public ClaimantDefendantAgreedMediationNotifier(NotificationService notificationService,
                                                    NotificationsProperties notificationsProperties,
                                                    OrganisationService organisationService,
                                                    SimpleStateFlowEngine stateFlowEngine,
                                                    CaseTaskTrackingService caseTaskTrackingService,
                                                    FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    @Override
    protected String getTaskId() {
        return ClaimantDefendantAgreedMediationNotify.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        String template =  featureToggleService.isCarmEnabledForCase(caseData)
            ? notificationsProperties.getNotifyApplicantLRMediationTemplate()
            : notificationsProperties.getNotifyApplicantLRMediationAgreementTemplate();

        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));

        return EmailDTO.builder()
            .targetEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
            .emailTemplate(template)
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_APPLICANT, caseData.getLegacyCaseReference()))
            .build();
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        recipients.add(getRespondent(caseData, true));
        if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)) {
            recipients.add(getRespondent(caseData, false));
        }

        return recipients;
    }

    private EmailDTO getRespondent(CaseData caseData, boolean isRespondent1) {
        String respondentEmail;
        String template;
        boolean sendMediationNotifiction;
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);

        if (isRespondent1) {
            respondentEmail = caseData.isRespondent1NotRepresented() ? caseData.getRespondent1PartyEmail() :
                caseData.getRespondentSolicitor1EmailAddress();
            sendMediationNotifiction = shouldSendMediationNotificationDefendant1LRCarm(caseData, carmEnabled);
        } else {
            respondentEmail = caseData.isRespondent2NotRepresented() ? caseData.getRespondent2PartyEmail() :
                caseData.getRespondentSolicitor2EmailAddress();
            sendMediationNotifiction = shouldSendMediationNotificationDefendant2LRCarm(caseData, carmEnabled);
        }

        if (!sendMediationNotifiction && !isRespondent1) {
            return null;
        } else if (sendMediationNotifiction) {
            template = notificationsProperties.getNotifyDefendantLRForMediation();
        } else if (caseData.isRespondent1NotRepresented()) {
            template = caseData.isRespondentResponseBilingual()
                ? notificationsProperties.getNotifyRespondentLiPMediationAgreementTemplateWelsh() :
                notificationsProperties.getNotifyRespondentLiPMediationAgreementTemplate();
        } else {
            template = notificationsProperties.getNotifyRespondentLRMediationAgreementTemplate();
        }

        OrganisationPolicy organisationPolicy = isRespondent1 ? caseData.getRespondent1OrganisationPolicy() :
            caseData.getRespondent2OrganisationPolicy();

        Map<String, String> properties = addProperties(caseData);
        if (isRespondent1) {
            properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        }

        if (caseData.isRespondent1NotRepresented()) {
            properties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        } else {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC,
                           getRespondentLegalOrganizationName(organisationPolicy, organisationService));
        }

        return EmailDTO.builder()
            .targetEmail(respondentEmail)
            .emailTemplate(template)
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
            .build();
    }
}
