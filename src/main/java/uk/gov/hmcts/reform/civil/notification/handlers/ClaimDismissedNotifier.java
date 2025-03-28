package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimDismissedNotifyParties;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
@Slf4j
public class ClaimDismissedNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE_APPLICANT_FOR_CLAIM_DISMISSED = "claim-dismissed-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED = "claim-dismissed-respondent-notification-%s";

    protected ClaimDismissedNotifier(NotificationService notificationService,
                                     NotificationsProperties notificationsProperties,
                                     OrganisationService organisationService,
                                     SimpleStateFlowEngine stateFlowEngine,
                                     CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    public String getTaskId() {
        return ClaimDismissedNotifyParties.toString();
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
    @NotNull
    protected Set<EmailDTO> getPartiesToNotify(final CaseData caseData) {
        log.info("Building list of recipients for Claim Dismissed event. Case id - {}", caseData.getCcdCaseReference());

        Set<EmailDTO> partiesToEmail = new HashSet<>();
        String stateName = stateFlowEngine.evaluate(caseData).getState().getName();

        if (CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName().equals(stateName)) {
            partiesToEmail.addAll(getRespondents(caseData));
        }
        partiesToEmail.addAll(getApplicants(caseData));
        return partiesToEmail;
    }

    private Set<EmailDTO> getApplicants(CaseData caseData) {
        return new HashSet<>(Set.of(getApplicant(caseData)));
    }

    private EmailDTO getApplicant(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return EmailDTO.builder()
                .targetEmail(caseData.getApplicantSolicitor1UserDetails().getEmail())
                .emailTemplate(getEmailTemplateId(caseData))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE_APPLICANT_FOR_CLAIM_DISMISSED,
                        caseData.getLegacyCaseReference()))
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
                .emailTemplate(getEmailTemplateId(caseData))
                .parameters(properties)
                .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED,
                        caseData.getLegacyCaseReference()))
                .build();
    }

    private String getEmailTemplateId(CaseData caseData) {
        return NotificationUtils.getSolicitorClaimDismissedProperty(
            stateFlowEngine.evaluate(caseData)
                .getStateHistory()
                .stream()
                .map(State::getName)
                .toList(),
            notificationsProperties
        );
    }
}
