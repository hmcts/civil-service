package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
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

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AcknowledgeClaimUnspecNotifyParties;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseIntentionForEmail;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.isAcknowledgeUserRespondentTwo;

@Component
public class AcknowledgeClaimUnspecNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE_APPLICANT = "acknowledge-claim-applicant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "acknowledge-claim-respondent-notification-%s";

    public AcknowledgeClaimUnspecNotifier(NotificationService notificationService,
                                          NotificationsProperties notificationsProperties,
                                          OrganisationService organisationService,
                                          SimpleStateFlowEngine stateFlowEngine,
                                          CaseTaskTrackingService caseTaskTrackingService) {
        super(
            notificationService,
            notificationsProperties,
            organisationService,
            stateFlowEngine,
            caseTaskTrackingService
        );
    }

    @Override
    protected String getTaskId() {
        return AcknowledgeClaimUnspecNotifyParties.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Map<String, String> properties = new HashMap<>(addProperties(caseData));
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getRespondent(caseData, properties));
        partiesToEmail.add(getApplicant(caseData, properties));
        return partiesToEmail;
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

    private EmailDTO getApplicant(CaseData caseData, Map<String, String> properties) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        return EmailDTO.builder()
            .targetEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
            //Template is common for applicant and respondent
            .emailTemplate(notificationsProperties.getRespondentSolicitorAcknowledgeClaim())
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_APPLICANT, caseData.getLegacyCaseReference()))
            .build();
    }

    private EmailDTO getRespondent(CaseData caseData, Map<String, String> properties) {
        boolean isRespondent1Acknowledged = isRespondentOneAcknowledged(caseData);
        Party respondent = getAcknowledgedRespondent(caseData, isRespondent1Acknowledged);
        LocalDateTime responseDeadline = getResponseDeadline(caseData, isRespondent1Acknowledged);

        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                                                                                        isRespondent1Acknowledged, organisationService));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(respondent));
        properties.put(RESPONSE_DEADLINE, formatLocalDate(responseDeadline.toLocalDate(), DATE));

        String respondentSolicitorEmailAddress = getRespondentSolicitorEmail(caseData, isRespondent1Acknowledged);

        return EmailDTO.builder()
            .targetEmail(respondentSolicitorEmailAddress)
            .emailTemplate(notificationsProperties.getRespondentSolicitorAcknowledgeClaim())
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
            .build();
    }

    private Party getAcknowledgedRespondent(CaseData caseData, boolean isRespondent1Acknowledged) {
        return isRespondent1Acknowledged ? caseData.getRespondent1()
            : caseData.getRespondent2();
    }

    private LocalDateTime getResponseDeadline(CaseData caseData, boolean isRespondent1Acknowledged) {
        return isRespondent1Acknowledged ? caseData.getRespondent1ResponseDeadline()
            : caseData.getRespondent2ResponseDeadline();
    }

    private boolean isRespondentOneAcknowledged(CaseData caseData) {
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        return !(multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP && isAcknowledgeUserRespondentTwo(caseData));
    }

    private String getRespondentSolicitorEmail(CaseData caseData, boolean isRespondent1Acknowledged) {
        return isRespondent1Acknowledged ? caseData.getRespondentSolicitor1EmailAddress()
            : caseData.getRespondentSolicitor2EmailAddress();
    }
}
