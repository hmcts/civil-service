package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getSolicitorClaimDismissedProperty;

@Component
public class ClaimDismissedNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE_RESPONDENT = "claim-dismissed-respondent-notification-%s";
    private static final String REFERENCE_TEMPLATE_APPLICANT = "claim-dismissed-applicant-notification-%s";

    public ClaimDismissedNotifier(NotificationService notificationService,
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

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return EmailDTO.builder()
            .targetEmail(caseData.getApplicantSolicitor1UserDetails().getEmail())
            .emailTemplate(getEmailTemplate(caseData))
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
        Map<String, String> properties = addProperties(caseData);
        OrganisationPolicy organisationPolicy = isRespondent1 ? caseData.getRespondent1OrganisationPolicy() : caseData.getRespondent2OrganisationPolicy();
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(organisationPolicy, organisationService));

        return EmailDTO.builder()
            .targetEmail(isRespondent1 ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress())
            .emailTemplate(getEmailTemplate(caseData))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
            .build();
    }

    private String getEmailTemplate(CaseData caseData) {
        return getSolicitorClaimDismissedProperty(
            stateFlowEngine.evaluate(caseData).getStateHistory()
                .stream()
                .map(State::getName)
                .toList(),
            notificationsProperties
        );
    }
}
