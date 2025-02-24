package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseIntentionForEmail;

@Component
@Slf4j
public class AcknowledgeClaimNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE = "acknowledge-claim-applicant-notification-%s";

    public AcknowledgeClaimNotifier(NotificationService notificationService,
                                    NotificationsProperties notificationsProperties,
                                    OrganisationService organisationService,
                                    SimpleStateFlowEngine stateFlowEngine) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine);
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.add(getRespondent(caseData));

        return partiesToEmail;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();
        Party respondent = caseData.getRespondent1();
        //finding response deadline date for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                responseDeadline = caseData.getRespondent2ResponseDeadline();
                respondent = caseData.getRespondent2();
            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() == null)) {
                responseDeadline = caseData.getRespondent1ResponseDeadline();
                respondent = caseData.getRespondent1();
            } else if (caseData.getRespondent1AcknowledgeNotificationDate() != null) {
                if (caseData.getRespondent2AcknowledgeNotificationDate()
                        .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                    responseDeadline = caseData.getRespondent2ResponseDeadline();
                    respondent = caseData.getRespondent2();
                } else {
                    responseDeadline = caseData.getRespondent1ResponseDeadline();
                    respondent = caseData.getRespondent1();
                }
            }
        }

        return new HashMap<>(Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(respondent),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                RESPONSE_DEADLINE, formatLocalDate(responseDeadline.toLocalDate(), DATE),
                RESPONSE_INTENTION, getResponseIntentionForEmail(caseData)
        ));
    }

    private EmailDTO getApplicant(CaseData caseData) {
        Map<String, String> notificationProperties = addProperties(caseData);
        notificationProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return EmailDTO.builder()
                .targetEmail(caseData.getApplicantSolicitor1UserDetails().getEmail())
                .emailTemplate(notificationsProperties.getRespondentSolicitorAcknowledgeClaim())
                .parameters(notificationProperties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    private EmailDTO getRespondent(CaseData caseData) {
        Map<String, String> notificationProperties = addProperties(caseData);
        notificationProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService));
        return EmailDTO.builder()
                .targetEmail(getRespondentSolicitorEmailAddress(caseData))
                .emailTemplate(notificationsProperties.getRespondentSolicitorAcknowledgeClaim())
                .parameters(notificationProperties)
                .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
                .build();
    }

    private String getRespondentSolicitorEmailAddress(CaseData caseData) {
        String respondentSolicitorEmailAddress = caseData.getRespondentSolicitor1EmailAddress();

        //finding email for the correct respondent in a 1v2 different solicitor scenario
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                respondentSolicitorEmailAddress = caseData.getRespondentSolicitor2EmailAddress();
            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() == null)) {
                respondentSolicitorEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
            } else if (caseData.getRespondent1AcknowledgeNotificationDate() != null) {
                if (caseData.getRespondent2AcknowledgeNotificationDate()
                        .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                    respondentSolicitorEmailAddress = caseData.getRespondentSolicitor2EmailAddress();
                } else {
                    respondentSolicitorEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
                }
            }
        }
        return respondentSolicitorEmailAddress;
    }
}