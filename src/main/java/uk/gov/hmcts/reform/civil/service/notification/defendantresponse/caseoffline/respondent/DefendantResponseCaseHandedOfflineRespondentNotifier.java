package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;

@Component
@RequiredArgsConstructor
public class DefendantResponseCaseHandedOfflineRespondentNotifier implements NotificationData {

    private static final String REFERENCE_TEMPLATE =
        "defendant-response-case-handed-offline-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    public enum CaseHandledOfflineRecipient {
        RESPONDENT_SOLICITOR1,
        RESPONDENT_SOLICITOR2
    }


    public CallbackResponse notifyRespondentSolicitorForCaseHandedOffline(CaseData caseData, CaseHandledOfflineRecipient recipientType) {

        String recipientEmailAddress;
        String templateID;

        //Use 1v1 Template
        if (is1v1Or2v1Case(caseData)) {
            recipientEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
            templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline();
        } else {
            //Use Multiparty Template as there are 2 defendant responses
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                && !RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && !RespondentResponseTypeSpec.COUNTER_CLAIM
                .equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                && SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                templateID = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
            } else {
                templateID = notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty();
            }
            if (recipientType.equals(CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR1)) {
                recipientEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
            } else {
                recipientEmailAddress = caseData.getRespondentSolicitor2EmailAddress();
            }

            if (null == recipientEmailAddress && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
                recipientEmailAddress = caseData.getRespondentSolicitor1EmailAddress();
            }
        }

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (RespondentResponseTypeSpec.COUNTER_CLAIM.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && (caseData.getRespondent2() == null || YES.equals(caseData.getRespondentResponseIsSame()))) {
                sendNotificationToSolicitorSpecCounterClaim(caseData, recipientEmailAddress, recipientType);
            } else if (MultiPartyScenario.getMultiPartyScenario(caseData)
                .equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                && (caseData.getRespondent1ResponseDate() == null || caseData.getRespondent2ResponseDate() == null
                || CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR2.equals(recipientType))) {
                sendNotificationToSolicitorSpec(caseData, recipientEmailAddress, recipientType);
            }
        } else {
            sendNotificationToSolicitor(caseData, recipientEmailAddress, templateID);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void sendNotificationToSolicitor(CaseData caseData, String recipientEmailAddress, String templateID) {
        notificationService.sendMail(
            recipientEmailAddress,
            templateID,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return NotificationUtils.caseOfflineNotificationAddProperties(caseData);
    }

    private void sendNotificationToSolicitorSpecCounterClaim(CaseData caseData,
                                                             String recipientEmailAddress, CaseHandledOfflineRecipient recipientType) {
        String emailTemplate = notificationsProperties.getRespondentSolicitorCounterClaimForSpec();
        notificationService.sendMail(
            recipientEmailAddress,
            emailTemplate,
            addPropertiesSpec(caseData, recipientType),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    private void sendNotificationToSolicitorSpec(CaseData caseData,
                                                 String recipientEmailAddress, CaseHandledOfflineRecipient recipientType) {
        String emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        notificationService.sendMail(
            recipientEmailAddress,
            emailTemplate,
            addPropertiesSpec1v2DiffSol(caseData, recipientType),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
    }

    public Map<String, String> addPropertiesSpec(CaseData caseData, CaseHandledOfflineRecipient recipientType) {
        return Map.of(
            DEFENDANT_NAME_SPEC, getLegalOrganisationName(caseData, recipientType),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> addPropertiesSpec1v2DiffSol(CaseData caseData, CaseHandledOfflineRecipient recipientType) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, recipientType),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    private String getLegalOrganisationName(CaseData caseData, CaseHandledOfflineRecipient recipientType) {
        String organisationID;
        organisationID = CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR1.equals(recipientType)
            ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
            : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
