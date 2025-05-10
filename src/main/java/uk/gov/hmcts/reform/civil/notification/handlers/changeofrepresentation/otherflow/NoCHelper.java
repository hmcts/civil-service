package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.RecipientData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.COURT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME_WITH_SPACE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REFERENCE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
@AllArgsConstructor
public class NoCHelper {

    protected static final String LIP = "LiP";
    protected static final String REFERENCE_TEMPLATE = "notice-of-change-%s";

    private final OrganisationService organisationService;

    public Map<String, String> getProperties(CaseData caseData, boolean isOtherSolicitor2) {
        return Map.of(
            CASE_NAME, getCaseName(caseData),
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, caseData.getCcdCaseReference().toString(),
            FORMER_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToRemoveID()),
            NEW_SOL, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            OTHER_SOL_NAME, getOtherSolicitorOrgName(caseData, isOtherSolicitor2),
            LEGAL_REP_NAME_WITH_SPACE, getOrganisationName(caseData.getChangeOfRepresentation().getOrganisationToAddID()),
            REFERENCE, caseData.getCcdCaseReference().toString()
        );
    }

    public Map<String, String> getClaimantLipProperties(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_INTERIM, caseData.getRespondent1().getPartyName(),
            CLAIM_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public Map<String, String> getHearingFeeEmailProperties(CaseData caseData) {
        return Map.of(
            LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService),
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            COURT_LOCATION, caseData.getHearingLocation().getValue().getLabel(),
            HEARING_TIME, caseData.getHearingTimeHourMinute(),
            HEARING_FEE, String.valueOf(caseData.getHearingFee().formData()),
            HEARING_DUE_DATE, formatLocalDate(caseData.getHearingDueDate(), DATE)
        );
    }

    private String getOrganisationName(String orgId) {
        return Optional.ofNullable(orgId)
            .map(id -> organisationService.findOrganisationById(id)
                .orElseThrow(() -> new CallbackException("Invalid organisation ID: " + id)).getName())
            .orElse(LIP);
    }

    private String getOtherSolicitorOrgName(CaseData caseData, boolean isOtherSolicitor2) {
        String orgId = isOtherSolicitor2
            ? getOtherSolicitor2Name(caseData)
            : getOtherSolicitor1Name(caseData);
        return getOrganisationName(orgId);
    }

    public boolean isHearingFeePaid(CaseData caseData) {
        PaymentDetails details = caseData.getHearingFeePaymentDetails();
        return details != null && PaymentStatus.SUCCESS.equals(details.getStatus());
    }

    public String getCaseName(CaseData caseData) {
        String applicants = caseData.getApplicant1().getPartyName();
        if (caseData.getApplicant2() != null) {
            applicants += String.format(", %s", caseData.getApplicant2().getPartyName());
        }
        String defendants = caseData.getRespondent1().getPartyName();
        if (caseData.getRespondent2() != null) {
            defendants += String.format(", %s", caseData.getRespondent2().getPartyName());
        }

        return String.format("%s v %s", applicants, defendants);
    }

    public String getOtherSolicitor2Name(CaseData caseData) {
        RecipientData otherSolicitor2 = getOtherSolicitor2(caseData);
        if (otherSolicitor2 != null) {
            return otherSolicitor2.getOrgId();
        }
        return null;
    }

    /**
     * Gets the other solicitor 2 to compliment other solicitor 1 if its a 1v1. if its a 2v1 it'll get the applicant 1
     * solicitor as oppose to applicant2 as theyre the same solicitor.
     * @param caseData data that contains which party has changed
     * @return Recipient data model that contains a String email and a String Organisation id, else returns null if
     *     the part is a LiP.
     */
    protected RecipientData getOtherSolicitor2(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            if (!isOtherPartyLip(caseData.getRespondent1OrganisationPolicy())) {
                String respondent1OrgID = Optional.ofNullable(caseData.getRespondent1OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(Organisation::getOrganisationID)
                    .orElse(caseData.getRespondent1OrganisationIDCopy());
                return RecipientData.builder()
                    .email(caseData.getRespondentSolicitor1EmailAddress())
                    .orgId(respondent1OrgID)
                    .build();
            }
        } else {
            if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
                String applicantOrgId = Optional.ofNullable(caseData.getApplicant1OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(Organisation::getOrganisationID)
                    .orElse(null);
                return RecipientData.builder()
                    .email(caseData.getApplicantSolicitor1UserDetailsEmail())
                    .orgId(applicantOrgId)
                    .build();
            } else {
                if (!isOtherPartyLip(caseData.getRespondent2OrganisationPolicy())) {
                    String respondent2OrgID = Optional.ofNullable(caseData.getRespondent2OrganisationPolicy())
                        .map(OrganisationPolicy::getOrganisation)
                        .map(Organisation::getOrganisationID)
                        .orElse(caseData.getRespondent2OrganisationIDCopy());
                    return RecipientData.builder()
                        .email(caseData.getRespondentSolicitor2EmailAddress())
                        .orgId(respondent2OrgID)
                        .build();
                }
            }
        }
        return null;
    }

    private boolean isRespondent2NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
    }

    /**
     * checks if the organisation policy is null to determine if the party is LiP or not.
     * @param organisationToCheck Object to check null
     * @return true if the organisation is null meaning the party is a LiP
     */
    protected boolean isOtherPartyLip(OrganisationPolicy organisationToCheck) {
        return organisationToCheck == null
            || organisationToCheck.getOrganisation() == null;
    }

    /**
     * checks the value of the other solicitor 2 to determine if the other party lip check was true or not to skip
     * the notification process.
     * @param caseData data containing the other solicitor 2
     * @return true if the other party 2 is a LiP
     */
    public boolean isOtherParty2Lip(CaseData caseData) {
        return getOtherSolicitor2(caseData) == null;
    }

    /**
     * checks the value of the other solicitor 1 to determine if the other party lip check was true or not to skip
     * the notification process.
     * @param caseData data containing the other solicitor 1
     * @return true if the other party 1 is a LiP
     */
    public boolean isOtherParty1Lip(CaseData caseData) {
        return getOtherSolicitor1(caseData) == null;
    }

    public String getOtherSolicitor1Name(CaseData caseData) {
        RecipientData otherSolicitor1 = getOtherSolicitor1(caseData);
        if (otherSolicitor1 != null) {
            return otherSolicitor1.getOrgId();
        }
        return null;
    }

    /**
     * will return applicant 1 details if the changed solicitor isnt applicant one.
     * @param caseData data with the changed solicitor.
     * @return Recipient data model that contains a String email and a String Organisation id, else returns null if
     *     the part is a LiP.
     */
    protected RecipientData getOtherSolicitor1(CaseData caseData) {
        if (isRespondent2NewSolicitor(caseData)) {
            String applicantOrgId = Optional.ofNullable(caseData.getApplicant1OrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .map(Organisation::getOrganisationID)
                .orElse(null);
            return RecipientData.builder()
                .email(caseData.getApplicantSolicitor1UserDetailsEmail())
                .orgId(applicantOrgId)
                .build();
        } else if (isApplicant1NewSolicitor(caseData)) {
            if (!isOtherPartyLip(caseData.getRespondent1OrganisationPolicy())) {
                String respondent1OrgID = Optional.ofNullable(caseData.getRespondent1OrganisationPolicy())
                    .map(OrganisationPolicy::getOrganisation)
                    .map(Organisation::getOrganisationID)
                    .orElseGet(caseData::getRespondent1OrganisationIDCopy);
                return RecipientData.builder()
                    .email(caseData.getRespondentSolicitor1EmailAddress())
                    .orgId(respondent1OrgID)
                    .build();
            }
        } else if (isRespondent1NewSolicitor(caseData) && !caseData.isApplicantLipOneVOne()) {
            String applicantOrgId = Optional.ofNullable(caseData.getApplicant1OrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .map(Organisation::getOrganisationID)
                .orElse(null);
            return RecipientData.builder()
                .email(caseData.getApplicantSolicitor1UserDetailsEmail())
                .orgId(applicantOrgId)
                .build();
        }
        return null;
    }

    protected boolean isApplicant1NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
    }

    protected boolean isRespondent1NewSolicitor(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getCaseRole()
            .equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
    }

    public boolean isApplicantLipForRespondentSolicitorChange(CaseData caseData) {
        return caseData.isApplicantLipOneVOne() && isRespondent1NewSolicitor(caseData);
    }

    /**
     * Get Change of representation former email address.
     * @param caseData data with change of representation in.
     * @return string email of the former solicitor.
     */
    public String getPreviousSolicitorEmail(CaseData caseData) {
        return caseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress();
    }

    public String getOtherSolicitor1Email(CaseData caseData) {
        RecipientData otherSolicitor1 = getOtherSolicitor1(caseData);
        if (otherSolicitor1 != null) {
            return otherSolicitor1.getEmail();
        }
        if (caseData.isApplicantLipOneVOne() && isRespondent1NewSolicitor(caseData)) {
            return caseData.getApplicant1Email();
        }
        return null;
    }

    public String getOtherSolicitor2Email(CaseData caseData) {
        RecipientData otherSolicitor2 = getOtherSolicitor2(caseData);
        if (otherSolicitor2 != null) {
            return otherSolicitor2.getEmail();
        }
        return null;
    }
}
