package uk.gov.hmcts.reform.civil.service.notification.defendantresponse;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
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

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.toStringValueForEmail;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@RequiredArgsConstructor
public class DefendantResponseApplicantSolicitorOneSpecNotifier implements NotificationData {

    //NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE
    private static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    private void notifySolicitorsForDefendantResponse(CaseData caseData, CaseEvent caseEvent) {
        String recipient;
        recipient = getRecipient(caseData);
        sendNotificationToSolicitorSpec(caseData, recipient, caseEvent);

    }

    @Nullable
    protected String getRecipient(CaseData caseData) {
        YesOrNo applicant1Represented = caseData.getApplicant1Represented();
        return NO.equals(applicant1Represented) ? caseData.getApplicant1().getPartyEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();

    }

    protected void sendNotificationToSolicitorSpec(CaseData caseData, String recipient, CaseEvent caseEvent) {
        String emailTemplate;

        emailTemplate = getEmailTemplate(caseData);
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addPropertiesSpec(caseData, caseEvent),
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


    protected Map<String, String> addPropertiesSpec(CaseData caseData, CaseEvent caseEvent) {

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
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                WHEN_WILL_BE_PAID_IMMEDIATELY, shouldBePaidBy
            );
        } else {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getApplicant1())
            );
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        if (getMultiPartyScenario(caseData).equals(ONE_V_ONE) || getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PARTY_REFERENCES, buildPartiesReferences(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
            );
        } else {
            //if there are 2 respondents on the case, concatenate the names together for the template subject line
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONDENT_NAME,
                getPartyNameBasedOnType(caseData.getRespondent1())
                    .concat(" and ")
                    .concat(getPartyNameBasedOnType(caseData.getRespondent2())),
                PARTY_REFERENCES, buildPartiesReferences(caseData),
                ALLOCATED_TRACK, toStringValueForEmail(caseData.getAllocatedTrack())
            );
        }
    }


    //finding legal org name
    private String getLegalOrganisationName(CaseData caseData, CaseEvent caseEvent) {
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
