package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
@RequiredArgsConstructor
public class FullDefenceApplicantSolicitorOneSpecNotifier extends FullDefenceSolicitorNotifier {

    //NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE
    private final NotificationsProperties notificationsProperties;
    private final NotificationService notificationService;
    private final OrganisationService organisationService;

    @Override
    protected String getRecipient(CaseData caseData) {
        YesOrNo applicant1Represented = caseData.getApplicant1Represented();
        return NO.equals(applicant1Represented) ? caseData.getApplicant1Email()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected void sendNotificationToSolicitor(CaseData caseData, String recipient) {
        String emailTemplate;
        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
        ) {
            emailTemplate = notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec();
        } else {
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
            } else if (caseData.isApplicant1NotRepresented()
                && RespondentResponseTypeSpec.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                if (caseData.isClaimantBilingual()) {
                    emailTemplate =
                        notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate();
                } else {
                    emailTemplate = notificationsProperties.getClaimantLipClaimUpdatedTemplate();
                }
            } else {
                emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponseForSpec();
            }
        }
        notificationService.sendMail(
            recipient,
            emailTemplate,
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
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
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                WHEN_WILL_BE_PAID_IMMEDIATELY, shouldBePaidBy,
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData)
            );
        } else {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData),
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
            );
        }
    }

    private String getLegalOrganisationName(CaseData caseData) {
        YesOrNo applicant1Represented = caseData.getApplicant1Represented();
        if (NO.equals(applicant1Represented)) {
            return caseData.getApplicant1().getPartyName();
        }
        String organisationID = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
        return organisation.isPresent() ? organisation.get().getName() :
            caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
    }
}
