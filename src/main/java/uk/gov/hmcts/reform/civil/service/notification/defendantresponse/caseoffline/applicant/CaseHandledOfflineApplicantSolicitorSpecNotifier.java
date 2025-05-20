package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addCommonFooterSignature;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addLipContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.addSpecAndUnspecContact;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class CaseHandledOfflineApplicantSolicitorSpecNotifier extends CaseHandledOfflineApplicantSolicitorNotifier {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Autowired
    public CaseHandledOfflineApplicantSolicitorSpecNotifier(NotificationService notificationService, NotificationsProperties notificationsProperties,
                                                            OrganisationService organisationService,
                                                            NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        super(notificationService, organisationService, configuration, featureToggleService);
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
    }

    public void notifyApplicantSolicitorForCaseHandedOffline(CaseData caseData) {
        String templateID;
        if (caseData.isLipvLROneVOne()) {
            sendNotificationToLiPApplicant(caseData);
        } else {
            String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
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

    }

    private void sendNotificationToLiPApplicant(CaseData caseData) {
        String emailTemplate = caseData.isClaimantBilingual()
            ? notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()
            : notificationsProperties.getClaimantLipClaimUpdatedTemplate();
        notificationService.sendMail(
            caseData.getApplicant1Email(),
            emailTemplate,
            addPropertiesLipApplicant(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
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
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_NAME_SPEC, getLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, getNotificationsSignatureConfiguration());
        addSpecAndUnspecContact(caseData, properties, getNotificationsSignatureConfiguration(),
                                getFeatureToggleService().isQueryManagementLRsEnabled());
        return properties;
    }

    public Map<String, String> addPropertiesLipApplicant(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
            ));
        addCommonFooterSignature(properties, getNotificationsSignatureConfiguration());
        addLipContact(caseData, properties, getFeatureToggleService().isQueryManagementLRsEnabled(),
                      getFeatureToggleService().isLipQueryManagementEnabled(caseData));
        return properties;
    }

    public Map<String, String> addPropertiesSpec1v2DiffSol(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
        addCommonFooterSignature(properties, getNotificationsSignatureConfiguration());
        addSpecAndUnspecContact(caseData, properties, getNotificationsSignatureConfiguration(),
                                getFeatureToggleService().isQueryManagementLRsEnabled());
        return properties;
    }

    private String getLegalOrganisationName(CaseData caseData) {
        String organisationID;
        organisationID = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
