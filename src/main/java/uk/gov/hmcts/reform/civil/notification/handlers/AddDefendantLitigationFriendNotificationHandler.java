package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddDefendantLitigationFriendNotificationHandler extends NotificationHandler implements NotificationData {

    protected static final String REFERENCE_TEMPLATE_APPLICANT = "litigation-friend-added-applicant-notification-%s";
    protected static final String REFERENCE_TEMPLATE_RESPONDENT = "litigation-friend-added-respondent-notification-%s";

    @Override
    public void notifyParties(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.addAll(getApplicants(caseData));
        partiesToEmail.addAll(getRespondents(caseData));

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
                .parameters(addProperties(caseData))
                .reference(String.format(REFERENCE_TEMPLATE_APPLICANT, caseData.getLegacyCaseReference()))
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
                        .parameters(addProperties(caseData))
                        .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
                        .build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
        ));
    }
}
