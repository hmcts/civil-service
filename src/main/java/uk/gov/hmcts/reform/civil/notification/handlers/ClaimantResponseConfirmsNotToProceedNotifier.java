package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantResponseConfirmsNotToProceedNotify;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseConfirmsNotToProceedNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-not-to-proceed-respondent-notification-%s";

    public ClaimantResponseConfirmsNotToProceedNotifier(NotificationService notificationService,
                                                        NotificationsProperties notificationsProperties,
                                                        OrganisationService organisationService,
                                                        SimpleStateFlowEngine stateFlowEngine,
                                                        CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData) {
        if (caseData.isPartAdmitPayImmediatelyAccepted()) {
            OrganisationPolicy organisationPolicy = caseData.getRespondent1OrganisationPolicy();
            return new HashMap<>(Map.of(
                CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(organisationPolicy, organisationService),
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
            ));
        }
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    @Override
    protected String getTaskId() {
        return ClaimantResponseConfirmsNotToProceedNotify.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        Map<String, String> properties;
        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            properties = addPropertiesSpec(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        } else {
            properties = addProperties(caseData);
        }

        return EmailDTO.builder()
            .targetEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
            .emailTemplate(getTemplate(caseData, true))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
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
        Map<String, String> properties;
        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            properties = addPropertiesSpec(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData, isRespondent1, organisationService));
        } else {
            properties = addProperties(caseData);
        }

        return EmailDTO.builder()
            .targetEmail(isRespondent1 ? caseData.getRespondentSolicitor1EmailAddress(): caseData.getRespondentSolicitor2EmailAddress())
            .emailTemplate(getTemplate(caseData, false))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    private String getTemplate(CaseData caseData, boolean isApplicant) {
        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            if (caseData.isPartAdmitPayImmediatelyAccepted()) {
                return notificationsProperties.getNotifyRespondentSolicitorPartAdmitPayImmediatelyAcceptedSpec();
            } else {
                return isApplicant ? notificationsProperties.getClaimantSolicitorConfirmsNotToProceedSpec()
                    : notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec();
            }
        }

        return notificationsProperties.getClaimantSolicitorConfirmsNotToProceed();
    }
}
