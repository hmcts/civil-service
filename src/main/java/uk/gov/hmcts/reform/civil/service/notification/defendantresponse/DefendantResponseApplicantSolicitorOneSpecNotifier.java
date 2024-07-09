package uk.gov.hmcts.reform.civil.service.notification.defendantresponse;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@RequiredArgsConstructor
public class DefendantResponseApplicantSolicitorOneSpecNotifier implements NotificationData {

    //NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE
    private static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    public void notifySolicitorsForDefendantResponse(CaseData caseData) {
        String recipient;
        recipient = getRecipient(caseData);
        sendNotificationToSolicitorSpec(caseData, recipient);
    }

    @Nullable
    protected String getRecipient(CaseData caseData) {
        YesOrNo applicant1Represented = caseData.getApplicant1Represented();
        return NO.equals(applicant1Represented) ? caseData.getApplicant1().getPartyEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();

    }

    protected void sendNotificationToSolicitorSpec(CaseData caseData, String recipient) {
        String emailTemplate;

        emailTemplate = getEmailTemplate(caseData);
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

    }

    private String getEmailTemplate(CaseData caseData) {
        String emailTemplate;
        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
        ) {
            emailTemplate = notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec();
        } else {
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
            } else {
                emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponseForSpec();
            }
        }
        return emailTemplate;
    }


    @Override
    public Map<String, String> addProperties(CaseData caseData) {

        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_ADMISSION.equals(
            caseData.getRespondent2ClaimResponseTypeForSpec()))
        ) {
            String shouldBePaidBy = caseData.getRespondToClaimAdmitPartLRspec()
                .getWhenWillThisAmountBePaid().getDayOfMonth()
                + " " + caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid().getMonth()
                + " " + caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid().getYear();
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                WHEN_WILL_BE_PAID_IMMEDIATELY, shouldBePaidBy
            );
        } else {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
            );
        }
    }

    //finding legal org name
    private String getLegalOrganisationName(CaseData caseData) {
        String organisationID;

        YesOrNo applicant1Represented = caseData.getApplicant1Represented();
        if (NO.equals(applicant1Represented)) {
            return caseData.getApplicant1().getPartyName();
        }
        organisationID = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();

        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }

}
