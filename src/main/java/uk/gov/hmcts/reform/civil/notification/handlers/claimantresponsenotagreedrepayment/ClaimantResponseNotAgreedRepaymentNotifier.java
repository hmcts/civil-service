package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.ClaimantResponseNotAgreedRepaymentNotify;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseNotAgreedRepaymentNotifier extends Notifier {

    private final FeatureToggleService featureToggleService;

    private static final String REFERENCE_TEMPLATE = "claimant-reject-repayment-respondent-notification-%s";

    public ClaimantResponseNotAgreedRepaymentNotifier(NotificationService notificationService,
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
        return (caseData.isApplicant1NotRepresented() && featureToggleService.isLipVLipEnabled())
            ? new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
        ))
            : new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    public Map<String, String> addPropertiesLip(CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
        ));
    }

    @Override
    protected String getTaskId() {
        return ClaimantResponseNotAgreedRepaymentNotify.toString();
    }

    @Override
    protected Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        return null;
    }

    private EmailDTO getApplicant(CaseData caseData) {
        boolean isApplicantLip = caseData.isApplicant1NotRepresented() && featureToggleService.isLipVLipEnabled();
        String email = isApplicantLip ?
            caseData.getApplicant1Email() : caseData.getApplicantSolicitor1UserDetails().getEmail();
        String emailTemplate = isApplicantLip ? notificationsProperties.getNotifyClaimantLipTemplateManualDetermination()
            : notificationsProperties.getNotifyClaimantLrTemplate();
        Map<String, String> properties = addProperties(caseData);

        if (!isApplicantLip) {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        }

        return EmailDTO.builder()
            .targetEmail(email)
            .emailTemplate(emailTemplate)
            .parameters(properties)
            .reference(String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()))
            .build();
    }

    private EmailDTO getRespondent(CaseData caseData, boolean isRespondent1) {
        String template;

        if (caseData.isRespondent1NotRepresented()) {
            template = caseData.isRespondentResponseBilingual() ? notificationsProperties.getNotifyDefendantLipWelshTemplate()
                : notificationsProperties.getNotifyDefendantLipTemplate();
        } else {
            template = notificationsProperties.getNotifyDefendantLrTemplate();
        }

        return EmailDTO.builder()
            .targetEmail(caseData.getRespondent1Email())
            .emailTemplate(template)
            .build();
    }
}
