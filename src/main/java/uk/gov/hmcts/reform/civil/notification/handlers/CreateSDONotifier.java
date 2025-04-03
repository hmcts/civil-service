package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.CreateSDONotify;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

public class CreateSDONotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE_RESPONDENT_1 = "create-sdo-respondent-1-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT_2 = "create-sdo-respondent-2-notification-%s";
    private static final String REFERENCE_TEMPLATE_APPLICANT = "create-sdo-applicants-notification-%s";

    private final FeatureToggleService featureToggleService;

    public CreateSDONotifier(NotificationService notificationService,
                             NotificationsProperties notificationsProperties,
                             OrganisationService organisationService,
                             SimpleStateFlowEngine stateFlowEngine,
                             CaseTaskTrackingService caseTaskTrackingService,
                             FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    public String getTaskId() {
        return CreateSDONotify.toString();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
        ));
    }

    public Map<String, String> addPropertiesLip(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        ));
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        boolean isLiP = caseData.isApplicantLiP();
        boolean isBilingual = caseData.isClaimantBilingual();

        Map<String, String> properties;

        if (isLiP) {
            properties = addPropertiesLip(caseData);
            properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
        } else {
            properties = addProperties(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        }

        String template = getTemplate(isLiP, isBilingual, false, caseData.getCaseAccessCategory(),
                                      caseData.getCaseManagementLocation().getBaseLocation());
        String email = isLiP ? caseData.getClaimantUserDetailsEmail()
            : caseData.getApplicantSolicitor1UserDetailsEmail();

        return EmailDTO.builder()
            .targetEmail(email)
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
        boolean isLiP = isRespondent1 ? caseData.isRespondent1LiP() : caseData.isRespondent2LiP();
        boolean isBilingual = caseData.isRespondentResponseBilingual();
        Map<String, String> properties;

        if (isLiP) {
            properties = addPropertiesLip(caseData);
            properties.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
        } else {
            String orgName = isRespondent1 ? caseData.getRespondent1().getPartyName() :
                caseData.getRespondent2().getPartyName();
            properties = addProperties(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, orgName);
        }

        String template = getTemplate(isLiP, isBilingual, true, caseData.getCaseAccessCategory(),
                                      caseData.getCaseManagementLocation().getBaseLocation());
        String reference = isRespondent1 ? REFERENCE_TEMPLATE_RESPONDENT_1 : REFERENCE_TEMPLATE_RESPONDENT_2;
        String email = isRespondent1 ? caseData.getRespondent1PartyEmail() : caseData.getRespondent2PartyEmail();

        return EmailDTO.builder()
            .targetEmail(email)
            .emailTemplate(template)
            .parameters(properties)
            .reference(String.format(reference, caseData.getLegacyCaseReference()))
            .build();
    }

    private String getTemplate(boolean isLiP, boolean isBilingual, boolean isRespondent,
                               CaseCategory caseCategory, String baseLocation) {
        if (isLiP) {
            return isBilingual ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        } else if (caseCategory == CaseCategory.SPEC_CLAIM) {
            if (isRespondent && isBilingual) {
                return notificationsProperties.getSdoOrderedSpecBilingual();
            } else if (isRespondent && featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(baseLocation)) {
                return notificationsProperties.getSdoOrderedSpecEa();
            }
            return notificationsProperties.getSdoOrderedSpec();
        }
        return notificationsProperties.getSdoOrdered();
    }
}
