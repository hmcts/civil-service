package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

@Component
public class CaseHandledOfflineApplicantSolicitorSpecNotifier extends CaseHandledOfflineApplicantSolicitorNotifier {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Autowired
    public CaseHandledOfflineApplicantSolicitorSpecNotifier(NotificationService notificationService, NotificationsProperties notificationsProperties,
                                                            OrganisationService organisationService) {
        super(notificationService, organisationService);
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
    }

    public void notifyApplicantSolicitorForCaseHandedOffline(CaseData caseData) {
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
        String templateID;

        if (is1v1Or2v1Case(caseData)) {
            templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
        } else {
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                templateID = notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
            } else {
                templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
            }
        }
        if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            && (caseData.getRespondent2() == null || YES.equals(caseData.getRespondentResponseIsSame()))) {
            sendNotificationToSolicitorSpecCounterClaim(caseData, recipient);
        } else if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            sendNotificationToSolicitorSpec(caseData, recipient);
        } else {
            sendNotificationToSolicitor(caseData, recipient, templateID);
        }

    }

    private void sendNotificationToSolicitorSpecCounterClaim(CaseData caseData, String recipient) {
        String emailTemplate = notificationsProperties.getClaimantSolicitorCounterClaimForSpec();
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addPropertiesSpec(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    private void sendNotificationToSolicitorSpec(CaseData caseData, String recipient) {
        String emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addPropertiesSpec1v2DiffSol(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData) {
        return Map.of(
            CLAIM_NAME_SPEC, getLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesSpec1v2DiffSol(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private String getLegalOrganisationName(CaseData caseData) {
        String organisationID;
        organisationID = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
