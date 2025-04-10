package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AcknowledgeSpecClaimNotifier;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseIntentionForEmail;

@Component
public class AcknowledgeClaimSpecNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE_APPLICANT = "acknowledge-claim-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "acknowledge-claim-respondent-notification-%s";

    public AcknowledgeClaimSpecNotifier(NotificationService notificationService,
                                        NotificationsProperties notificationsProperties,
                                        OrganisationService organisationService,
                                        SimpleStateFlowEngine stateFlowEngine,
                                        CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    public String getTaskId() {
        return AcknowledgeSpecClaimNotifier.toString();
    }

    @Override
    @NotNull
    protected Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
        Set<EmailDTO> emails = new HashSet<>();
        emails.add(getApplicant(caseData));
        emails.add(getRespondent(caseData));
        return emails;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                RESPONSE_INTENTION, getResponseIntentionForEmail(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    private EmailDTO getApplicant(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE));
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        String template = notificationsProperties.getApplicantSolicitorAcknowledgeClaimForSpec();
        return EmailDTO.builder()
                .targetEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
                .emailTemplate(template)
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE_APPLICANT, caseData.getLegacyCaseReference()))
                .build();
    }

    private EmailDTO getRespondent(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData, true, organisationService));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();
        properties.put(RESPONSE_DEADLINE, formatLocalDate(responseDeadline.toLocalDate(), DATE));
        String template = notificationsProperties.getRespondentSolicitorAcknowledgeClaimForSpec();
        return EmailDTO.builder()
                .targetEmail(caseData.getRespondentSolicitor1EmailAddress())
                .emailTemplate(template)
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
                .build();
    }
}
