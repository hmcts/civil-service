package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.BundleCreationNotify;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Component
public class BundleCreationNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE_APPLICANT = "bundle-created-applicant-notification-%s";

    private static final String REFERENCE_TEMPLATE_RESPONDENT =  "bundle-created-respondent-notification-%s";

    public BundleCreationNotifier(NotificationService notificationService,
                                  NotificationsProperties notificationsProperties,
                                  OrganisationService organisationService,
                                  SimpleStateFlowEngine stateFlowEngine,
                                  CaseTaskTrackingService caseTaskTrackingService
    ) {
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
        return BundleCreationNotify.toString();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    public Map<String, String> addPropertiesLip(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        ));
    }

    private String getTemplate(boolean isLip, boolean isLipWelsh) {
        if (isLip) {
            if (isLipWelsh) {
                return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else {
                return notificationsProperties.getNotifyLipUpdateTemplate();
            }
        } else {
            return notificationsProperties.getBundleCreationTemplate();
        }
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        boolean isApplicantLiP = NO.equals(caseData.getApplicant1Represented());
        boolean isApplicantWelsh = false;
        Map<String, String> properties;

        if (isApplicantLiP) {
            properties = addPropertiesLip(caseData);
            properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
            isApplicantWelsh = caseData.isClaimantBilingual();
        } else {
            properties = addProperties(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        }

        return EmailDTO.builder()
            .targetEmail(isApplicantLiP ? caseData.getApplicant1Email() : caseData.getApplicantSolicitor1UserDetailsEmail())
            .emailTemplate(getTemplate(isApplicantLiP, isApplicantWelsh))
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
        boolean isRespondentLiP;
        boolean isRespondentWelsh = false;
        Map<String, String> properties;
        String respondentEmail;

        if (isRespondent1) {
            isRespondentLiP = NO.equals(caseData.getRespondent1Represented());
            respondentEmail = isRespondentLiP ? caseData.getRespondent1PartyEmail() : caseData.getRespondentSolicitor1EmailAddress();
        } else {
            isRespondentLiP = NO.equals(caseData.getRespondent2Represented());
            respondentEmail = caseData.getRespondentSolicitor2EmailAddress();
        }

        if (isRespondentLiP) {
            properties = addPropertiesLip(caseData);
            properties.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
            isRespondentWelsh = caseData.isRespondentResponseBilingual();
        } else {
            properties = addProperties(caseData);
            OrganisationPolicy organisationPolicy = isRespondent1 ? caseData.getRespondent1OrganisationPolicy() : caseData.getRespondent2OrganisationPolicy();
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(organisationPolicy, organisationService));
        }

        return EmailDTO.builder()
            .targetEmail(respondentEmail)
            .emailTemplate(getTemplate(isRespondentLiP, isRespondentWelsh))
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE_RESPONDENT, caseData.getLegacyCaseReference()))
            .build();
    }
}
