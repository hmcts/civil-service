package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.Setter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
@Setter
public abstract class TrialReadyNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE = "other-party-trial-ready-notification-%s";

    protected static String TASK_ID;

    public TrialReadyNotifier(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        OrganisationService organisationService,
        SimpleStateFlowEngine stateFlowEngine,
        CaseTaskTrackingService caseTaskTrackingService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    private Map<String, String> addPropertiesLiP(boolean isApplicant, CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, isApplicant ? caseData.getApplicant1().getPartyName() : caseData.getRespondent1().getPartyName(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        switch (getTaskId()) {
            case "ApplicantNotifyOthersTrialReadyNotifier" -> {
                partiesToEmail.add(getRespondent(caseData, true));
                if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)) {
                    partiesToEmail.add(getRespondent(caseData, false));
                }
            }
            case "Respondent1NotifyOthersTrialReadyNotifier" -> {
                partiesToEmail.add(getApplicant(caseData));
                if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)) {
                    partiesToEmail.add(getRespondent(caseData, false));
                }
            }
            case "Respondent2NotifyOthersTrialReadyNotifier" -> {
                partiesToEmail.add(getApplicant(caseData));
                partiesToEmail.add(getRespondent(caseData, true));
            }
        }
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        boolean isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isApplicantNotRepresented();
        Map<String, String> properties;

        if (isLiP) {
            properties =  addPropertiesLiP(true, caseData);
        } else {
            properties = addProperties(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        }

        return EmailDTO.builder()
            .targetEmail(isLiP ? caseData.getApplicant1Email() : caseData.getApplicantSolicitor1UserDetails().getEmail())
            .emailTemplate(getTemplate(caseData, isLiP, true))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    private EmailDTO getRespondent(CaseData caseData, boolean isRespondent1) {
        boolean isLiP;
        String email;
        Map<String, String> properties;

        if (isRespondent1) {
            isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent1LiP();
            email = isLiP ? caseData.getRespondent1().getPartyEmail() : caseData.getRespondentSolicitor1EmailAddress();
        } else {
            isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent2LiP();
            email = isLiP ? caseData.getRespondent2().getPartyEmail() : caseData.getRespondentSolicitor2EmailAddress();
        }

        if (isLiP) {
            properties = addPropertiesLiP(false, caseData);
        } else {
            OrganisationPolicy organisationPolicy = isRespondent1 ? caseData.getRespondent1OrganisationPolicy() :
                caseData.getRespondent2OrganisationPolicy();
            properties = addProperties(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC,
                           getRespondentLegalOrganizationName(organisationPolicy, organisationService));
        }

        return EmailDTO.builder()
            .targetEmail(email)
            .emailTemplate(getTemplate(caseData, isLiP, false))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    private String getTemplate(CaseData caseData, boolean isLiP, boolean isApplicant) {
        String emailTemplate;
        if (isLiP) {
            if ((isApplicant && caseData.isClaimantBilingual())
                || caseData.isRespondentResponseBilingual()) {
                emailTemplate = notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else {
                emailTemplate = notificationsProperties.getNotifyLipUpdateTemplate();
            }
        } else {
            emailTemplate = notificationsProperties.getOtherPartyTrialReady();
        }
        return emailTemplate;
    }
}
