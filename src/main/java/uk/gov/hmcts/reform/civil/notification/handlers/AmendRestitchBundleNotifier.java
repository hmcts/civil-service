package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.AmendRestitchBundleNotify;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
@Slf4j
public class AmendRestitchBundleNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE_APPLICANT = "amend-restitch-bundle-claimant-notification-%s";
    private static final String REFERENCE_TEMPLATE_RESPONDENT = "amend-restitch-bundle-defendant-notification-%s";
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    public AmendRestitchBundleNotifier(NotificationService notificationService,
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
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
            BUNDLE_RESTITCH_DATE, LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
        ));
    }

    @Override
    protected String getTaskId() {
        return AmendRestitchBundleNotify.toString();
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
        properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());

        return EmailDTO.builder()
            .targetEmail(getApplicantEmail(caseData))
            .emailTemplate(getNotificationTemplate(caseData.isApplicantLiP(), caseData.isClaimantBilingual()))
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
        properties.put(PARTY_NAME, isRespondent1
            ? caseData.getRespondent1().getPartyName() : caseData.getRespondent2().getPartyName());

        return EmailDTO.builder()
            .targetEmail(getRespondentEmail(caseData, isRespondent1))
            .emailTemplate(getNotificationTemplate(caseData.isRespondent1LiP(), caseData.isRespondentResponseBilingual()))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
            .build();
    }

    private String getRespondentEmail(CaseData caseData, boolean isRespondent1) {
        if (caseData.isRespondent1LiP() && nonNull(caseData.getRespondent1().getPartyEmail())) {
            return caseData.getRespondent1().getPartyEmail();
        } else {
            return isRespondent1 ? caseData.getRespondentSolicitor1EmailAddress() :
                caseData.getRespondentSolicitor2EmailAddress();
        }
    }

    private String getNotificationTemplate(boolean isLiP, boolean isBilingual) {
        if (isLiP) {
            return isBilingual
                ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getNotifyLRBundleRestitched();
        }
    }

    private String getApplicantEmail(CaseData caseData) {
        return caseData.isApplicantLiP()
            ? caseData.getClaimantUserDetailsEmail()
            : caseData.getApplicantSolicitor1UserDetailsEmail();
    }
}
