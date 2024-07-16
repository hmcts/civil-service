package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import lombok.RequiredArgsConstructor;
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
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@RequiredArgsConstructor
public class FullDefenceApplicantSolicitorOneCCSpecNotifier extends FullDefenceSolicitorNotifier {

    //NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    public String getRecipient(CaseData caseData) {
        return caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        String emailTemplate;
        if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        } else {
            emailTemplate = getTemplateForSpecOtherThan1v2DS(caseData);
        }
        if (caseData.getRespondent1ResponseDate() == null || !MultiPartyScenario.getMultiPartyScenario(caseData)
            .equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            notificationService.sendMail(
                recipient,
                emailTemplate,
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
    }

    private String getTemplateForSpecOtherThan1v2DS(CaseData caseData) {
        String emailTemplate;
        if ((caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == BY_SET_DATE
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            &&
            (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
        ) {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction();
        } else {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        }
        return emailTemplate;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        } else {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent2())
            );
        }
    }

    //finding legal org name
    private String getLegalOrganisationName(CaseData caseData) {
        String organisationID;
        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            organisationID = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
        } else {
            organisationID = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();
        }

        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
