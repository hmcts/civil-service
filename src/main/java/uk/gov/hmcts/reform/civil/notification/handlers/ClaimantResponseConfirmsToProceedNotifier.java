package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantConfirmsToProceedNotify;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseConfirmsToProceedNotifier extends Notifier {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-respondent-notification-%s";
    private static final String NP_PROCEED_REFERENCE_TEMPLATE = "claimant-confirms-not-to-proceed-respondent-notification-%s";

    private final FeatureToggleService featureToggleService;

    public ClaimantResponseConfirmsToProceedNotifier(NotificationService notificationService,
                                                     NotificationsProperties notificationsProperties,
                                                     OrganisationService organisationService,
                                                     SimpleStateFlowEngine stateFlowEngine,
                                                     CaseTaskTrackingService caseTaskTrackingService,
                                                     FeatureToggleService featureToggleService) {
        super(notificationService, notificationsProperties, organisationService, stateFlowEngine, caseTaskTrackingService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    public Map<String, String> addPropertiesLRvLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private String getTemplate(CaseData caseData, boolean isApplicant, boolean isMultiPartyNotProceed) {
        if (isLRvLipToDefendant(caseData, isApplicant)) {
            return notificationsProperties.getRespondent1LipClaimUpdatedTemplate();
        } else if (isMultiPartyNotProceed) {
            return notificationsProperties.getClaimantSolicitorConfirmsNotToProceed();
        } else if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            boolean proceedsWithAction = rejectedAll(caseData) && mediationRejected(caseData);
            if (isApplicant) {
                return  proceedsWithAction ? notificationsProperties.getClaimantSolicitorConfirmsToProceedSpecWithAction()
                    : notificationsProperties.getClaimantSolicitorConfirmsToProceedSpec();
            }

            return proceedsWithAction ? notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction()
                : notificationsProperties.getRespondentSolicitorNotifyToProceedSpec();

        } else if (caseData.getAllocatedTrack().equals(MULTI_CLAIM) && !featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            return notificationsProperties.getSolicitorCaseTakenOffline();
        }

        return notificationsProperties.getClaimantSolicitorConfirmsToProceed();
    }

    private boolean rejectedAll(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE;
    }

    private boolean mediationRejected(CaseData caseData) {
        return Stream.of(
            caseData.getResponseClaimMediationSpecRequired(),
            caseData.getResponseClaimMediationSpec2Required(),
            Optional.ofNullable(caseData.getApplicant1ClaimMediationSpecRequired())
                .map(SmallClaimMedicalLRspec::getHasAgreedFreeMediation).orElse(null)
        ).filter(Objects::nonNull).anyMatch(YesOrNo.NO::equals);
    }

    private boolean isLRvLipToDefendant(CaseData caseData, boolean isApplicant) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && (caseData.isLRvLipOneVOne()
            || caseData.isLipvLipOneVOne())
            && !isApplicant;
    }

    private boolean isMultiPartyNotProceed(CaseData caseData, boolean isRespondent2) {
        return (NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()) && isRespondent2)
            || (NO.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()) && !isRespondent2);
    }

    @Override
    protected String getTaskId() {
        return ClaimantConfirmsToProceedNotify.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        partiesToEmail.add(getApplicant(caseData));
        partiesToEmail.addAll(getRespondents(caseData));
        return partiesToEmail;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        Map<String, String> properties = caseData.getCaseAccessCategory().equals(SPEC_CLAIM) ? addPropertiesSpec(caseData) : addProperties(caseData);
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        boolean isMultiPartyNotProceed = isMultiPartyNotProceed(caseData, false);

        String reference = isMultiPartyNotProceed ? String.format(NP_PROCEED_REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            : String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());

        return EmailDTO.builder()
            .targetEmail(caseData.getApplicantSolicitor1UserDetailsEmail())
            .emailTemplate(getTemplate(caseData, true, isMultiPartyNotProceed))
            .parameters(properties)
            .reference(reference)
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
        String email;
        Map<String, String> properties;

        if (isLRvLipToDefendant(caseData, false)) {
            email = caseData.getRespondent1().getPartyEmail();
            properties = addPropertiesLRvLip(caseData);
        } else {
            email = isRespondent1 ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress();
            properties = caseData.getCaseAccessCategory().equals(SPEC_CLAIM) ? addPropertiesSpec(caseData) : addProperties(caseData);
            OrganisationPolicy organisationPolicy = isRespondent1 ? caseData.getRespondent1OrganisationPolicy() : caseData.getRespondent2OrganisationPolicy();
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(organisationPolicy, organisationService));
        }

        boolean isMultiPartyNotProceed = isMultiPartyNotProceed(caseData, !isRespondent1);

        String reference = isMultiPartyNotProceed ? String.format(NP_PROCEED_REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            : String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());

        return EmailDTO.builder()
            .targetEmail(email)
            .emailTemplate(getTemplate(caseData, false, isMultiPartyNotProceed))
            .parameters(properties)
            .reference(reference)
            .build();
    }
}
