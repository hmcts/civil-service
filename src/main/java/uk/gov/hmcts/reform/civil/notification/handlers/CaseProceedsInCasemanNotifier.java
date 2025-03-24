package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
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

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class CaseProceedsInCasemanNotifier extends Notifier {

    private final FeatureToggleService featureToggleService;

    private static final String REFERENCE_TEMPLATE_APPLICANT = "case-proceeds-in-caseman-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "case-proceeds-in-caseman-respondent-notification-%s";

    public CaseProceedsInCasemanNotifier(NotificationService notificationService,
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
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        EmailDTO applicantDTO = getApplicant(caseData);
        if (applicantDTO !=  null) {
            partiesToEmail.add(applicantDTO);
        }

        Set<EmailDTO> respondentDTOs = getRespondents(caseData);
        if (respondentDTOs != null) {
            partiesToEmail.addAll(respondentDTOs);
        }

        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        if (caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()) {
            return null;
        }

        Map<String, String> properties;
        String email;

        if (caseData.isLipvLROneVOne()) {
            properties = addPropertiesForLip(caseData);
            email = caseData.getApplicant1Email();
        } else {
            properties = addProperties(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
            email = caseData.getApplicantSolicitor1UserDetails().getEmail();
        }

        return EmailDTO.builder()
            .targetEmail(email)
            .emailTemplate(getEmailTemplate(caseData))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_APPLICANT, caseData.getLegacyCaseReference()))
            .build();
    }

    private Set<EmailDTO> getRespondents(CaseData caseData) {
        if (!stateFlowEngine.hasTransitionedTo(caseData, CLAIM_NOTIFIED) &&
            !(stateFlowEngine.hasTransitionedTo(caseData, TAKEN_OFFLINE_BY_STAFF) && caseData.isLipvLROneVOne())) {
            return null;
        }

        Set<EmailDTO> recipients = new HashSet<>();
        recipients.add(getRespondent(caseData, true));
        if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)) {
            recipients.add(getRespondent(caseData, false));
        }

        return recipients;
    }

    private EmailDTO getRespondent(CaseData caseData, boolean isRespondent1) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC,
                       getLegalOrganizationNameForRespondent(caseData, isRespondent1, organisationService));

        return EmailDTO.builder()
            .targetEmail(isRespondent1 ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress())
            .emailTemplate(caseData.isLipvLROneVOne() ? notificationsProperties.getSolicitorCaseTakenOfflineForSpec() : notificationsProperties.getSolicitorCaseTakenOffline())
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
            .build();
    }

    private String getEmailTemplate(CaseData caseData) {
        if (caseData.isLipvLROneVOne()) {
            if (caseData.isClaimantBilingual()) {
                return notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate();
            }
            return notificationsProperties.getClaimantLipClaimUpdatedTemplate();
        }

        return notificationsProperties.getSolicitorCaseTakenOffline();
    }

    private Map<String, String> addPropertiesForLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }
}
