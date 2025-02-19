package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@Slf4j
public class BreathingSpaceLiftedNotifier extends Notifier implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "breathing-space-lifted-notification-%s";

    public BreathingSpaceLiftedNotifier(NotificationService notificationService,
                                        NotificationsProperties notificationsProperties,
                                        OrganisationService organisationService,
                                        SimpleStateFlowEngine stateFlowEngine) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
        ));
    }

    @Override
    protected EmailDTO getApplicant(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return EmailDTO.builder()
            .targetEmail(caseData.getApplicantSolicitor1UserDetails().getEmail())
            .emailTemplate(notificationsProperties.getBreathingSpaceLiftedApplicantEmailTemplate())
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    @Override
    protected Set<EmailDTO> getRespondents(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(
            caseData.getRespondent1OrganisationPolicy(), organisationService));

        Set<EmailDTO> recipients = new HashSet<>();
        recipients.add(EmailDTO.builder()
            .targetEmail(caseData.getRespondentSolicitor1EmailAddress())
            .emailTemplate(notificationsProperties.getBreathingSpaceLiftedApplicantEmailTemplate())
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build());

        return recipients;
    }
}
