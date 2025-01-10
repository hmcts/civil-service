package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.CaseHandledOfflineRecipient;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Component
@AllArgsConstructor
public class CaseHandledOfflineRespondentSolicitorSpecNotifier extends CaseHandledOfflineRespondentSolicitorNotifier {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    public void notifyRespondentSolicitorForCaseHandedOffline(CaseData caseData,
                                                              CaseHandledOfflineRecipient recipientType) {

        String recipientEmailAddress = getRecipientEmailAddress(caseData, recipientType);

        if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            && (caseData.getRespondent2() == null || YES.equals(caseData.getRespondentResponseIsSame()))) {
            sendNotificationToSolicitorSpecCounterClaim(caseData, recipientEmailAddress, recipientType);
        } else if (MultiPartyScenario.getMultiPartyScenario(caseData)
            .equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
            && (caseData.getRespondent1ResponseDate() == null || caseData.getRespondent2ResponseDate() == null
            || CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR2.equals(recipientType))) {
            sendNotificationToSolicitorSpec(caseData, recipientEmailAddress, recipientType);
        }
    }

    private void sendNotificationToSolicitorSpecCounterClaim(CaseData caseData,
                                                             String recipientEmailAddress,
                                                             CaseHandledOfflineRecipient recipientType) {
        String emailTemplate = notificationsProperties.getRespondentSolicitorCounterClaimForSpec();
        notificationService.sendMail(
            recipientEmailAddress,
            emailTemplate,
            addPropertiesSpec(caseData, recipientType),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    private void sendNotificationToSolicitorSpec(CaseData caseData,
                                                 String recipientEmailAddress,
                                                 CaseHandledOfflineRecipient recipientType) {
        String emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        notificationService.sendMail(
            recipientEmailAddress,
            emailTemplate,
            addPropertiesSpec1v2DiffSol(caseData, recipientType),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData,
                                                 CaseHandledOfflineRecipient recipientType) {
        return Map.of(
            DEFENDANT_NAME_SPEC, getLegalOrganisationName(caseData, recipientType),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesSpec1v2DiffSol(CaseData caseData,
                                                           CaseHandledOfflineRecipient recipientType) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, recipientType),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private String getLegalOrganisationName(CaseData caseData,
                                            CaseHandledOfflineRecipient recipientType) {
        String organisationID;
        organisationID = CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR1.equals(recipientType)
            ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
