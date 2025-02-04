package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
@Slf4j
public class ClaimDismissedNotifier extends Notifier {

    protected ClaimDismissedNotifier(NotificationService notificationService,
                                     NotificationsProperties notificationsProperties,
                                     OrganisationService organisationService, SimpleStateFlowEngine stateFlowEngine) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
        ));
    }

    @Override
    public void notifyParties(CaseData caseData) {
        log.info("Building list of recipients for Claim Dismissed event. Case id - {}", caseData.getCcdCaseReference());

        Set<EmailDTO> partiesToEmail = new HashSet<>();
        List<String> stateHistoryNameList = stateFlowEngine.evaluate(caseData).getStateHistory()
                .stream()
                .map(State::getName)
                .toList();

        if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName())) {
            partiesToEmail.addAll(getRespondents(caseData));
        }

        partiesToEmail.addAll(getApplicants(caseData));
        sendNotification(partiesToEmail);
    }

    private Set<EmailDTO> getApplicants(CaseData caseData) {
        return new HashSet<>(Set.of(getApplicant1(caseData)));
    }

    private EmailDTO getApplicant1(CaseData caseData) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return EmailDTO.builder()
                .targetEmail(caseData.getApplicantSolicitor1UserDetails().getEmail())
                .emailTemplate(notificationsProperties.getSolicitorLitigationFriendAdded())
                .parameters(properties)
                .reference(String.format(NotificationUtils.REFERENCE_TEMPLATE_APPLICANT_FOR_CLAIM_DISMISSED,
                        caseData.getLegacyCaseReference()))
                .build();
    }

    protected Set<EmailDTO> getRespondents(CaseData caseData) {
        Set<EmailDTO> recipients = new HashSet<>();
        recipients.add(getRespondent(caseData, true));
        if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)) {
            recipients.add(getRespondent(caseData, false));
        }

        return recipients;
    }

    protected EmailDTO getRespondent(CaseData caseData, boolean isRespondent1) {
        Map<String, String> properties = addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                isRespondent1, organisationService));

        return EmailDTO.builder()
                .targetEmail(caseData.getRespondentSolicitor1EmailAddress())
                .emailTemplate(notificationsProperties.getSolicitorLitigationFriendAdded())
                .parameters(properties)
                .reference(String.format(NotificationUtils.REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED,
                        caseData.getLegacyCaseReference()))
                .build();
    }
}
