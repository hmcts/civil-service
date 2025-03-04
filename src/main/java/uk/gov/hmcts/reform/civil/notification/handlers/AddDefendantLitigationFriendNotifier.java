package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.dashboard.repositories.NotificationExceptionRecordRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.model.NotificationParty.APPLICANT_1;
import static uk.gov.hmcts.reform.civil.model.NotificationParty.DEFENDANT_1;
import static uk.gov.hmcts.reform.civil.model.NotificationParty.DEFENDANT_2;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
@Slf4j
public class AddDefendantLitigationFriendNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE_APPLICANT = "litigation-friend-added-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "litigation-friend-added-respondent-notification-%s";

    public AddDefendantLitigationFriendNotifier(NotificationService notificationService,
                                                NotificationsProperties notificationsProperties,
                                                OrganisationService organisationService,
                                                SimpleStateFlowEngine stateFlowEngine,
                                                NotificationExceptionRecordRepository exceptionRecordRepository,
                                                CamundaRuntimeClient camundaRuntimeClient) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, exceptionRecordRepository, camundaRuntimeClient);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
        ));
    }

    @Override
    @NotNull
    protected Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
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
                .party(APPLICANT_1)
                .emailTemplate(notificationsProperties.getSolicitorLitigationFriendAdded())
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
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                isRespondent1, organisationService));

        return EmailDTO.builder()
                        .targetEmail(isRespondent1 ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress())
                        .emailTemplate(notificationsProperties.getSolicitorLitigationFriendAdded())
                        .parameters(properties)
                        .party(isRespondent1 ? DEFENDANT_1 : DEFENDANT_2)
                        .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
                        .build();
    }
}
