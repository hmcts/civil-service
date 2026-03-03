package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.math.BigDecimal.TEN;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseDataMinEdgeCasesBuilder extends CaseDataBuilder {

    public static CaseDataMinEdgeCasesBuilder builder() {
        return new CaseDataMinEdgeCasesBuilder();
    }

    public CaseDataMinEdgeCasesBuilder atStateProceedsOfflineUnrepresentedDefendantWithMinimalData() {
        atStatePaymentSuccessfulWithMinimalData();

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = NO;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = null;

        respondentSolicitor1OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg@email.com")
            .setOrganisationName("test org name")
            .setFax("123123123")
            .setDx("test org dx")
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.defaults().build())
            ;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateClaimDraftWithMinimalData() {
        courtLocation = new CourtLocation()
            .setApplicantPreferredCourt("127")
            .setApplicantPreferredCourtLocationList(
                DynamicList.builder().value(DynamicListElement.builder().label("sitename").build()).build())
            .setCaseLocation(new CaseLocationCivil()
                              .setRegion("4")
                              .setBaseLocation("000000")
                              );
        applicant1 = PartyBuilder.builder().companyWithMinimalData().build();
        applicant1LitigationFriendRequired = NO;
        applicantSolicitor1CheckEmail = new CorrectEmail()
            .setEmail("hmcts.civil@gmail.com")
            .setCorrect(YES);
        applicant1OrganisationPolicy = new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("QWERTY A"));
        respondent1OrganisationPolicy = new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("QWERTY R"));
        respondent1 = PartyBuilder.builder().companyWithMinimalData().build();
        respondent1Represented = NO;
        claimType = ClaimType.CLINICAL_NEGLIGENCE;
        claimValue = new ClaimValue()
            .setStatementOfValueInPennies(BigDecimal.valueOf(10000000));
        claimFee = new Fee()
            .setCalculatedAmountInPence(TEN)
            .setCode("fee code")
            .setVersion("version 1")
            ;
        paymentReference = "some reference";
        respondentSolicitor1EmailAddress = "respondentsolicitor@example.com";
        applicantSolicitor1UserDetails = new IdamUserDetails().setEmail("applicantsolicitor@example.com");
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.minimal();
        submittedDate = LocalDateTime.now();
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStatePendingCaseIssuedWithMinimalData() {
        atStateClaimDraftWithMinimalData();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateClaimIssuedWithMinimalData() {
        atStatePendingCaseIssuedWithMinimalData();
        issueDate = CLAIM_ISSUED_DATE;
        claimNotificationDeadline = NOTIFICATION_DEADLINE;
        ccdState = CASE_ISSUED;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStatePaymentSuccessfulWithMinimalData() {
        atStatePendingCaseIssuedWithMinimalData();
        claimIssuedPaymentDetails = new PaymentDetails()
            .setStatus(SUCCESS)
            .setReference("RC-1604-0739-2145-4711")
            ;
        paymentSuccessfulDate = LocalDateTime.now();
        claimDetailsNotificationDeadline = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateAwaitingCaseNotificationWithMinimalData() {
        atStatePaymentSuccessfulWithMinimalData();
        ccdState = CASE_ISSUED;
        issueDate = CLAIM_ISSUED_DATE;
        claimNotificationDeadline = LocalDateTime.now();
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateClaimNotifiedWithMinimumData() {
        atStateClaimIssuedWithMinimalData();
        claimNotificationDate = LocalDate.now().atStartOfDay();
        claimDetailsNotificationDeadline = DEADLINE;
        ccdState = AWAITING_CASE_DETAILS_NOTIFICATION;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateClaimDetailsNotifiedWithMinimumData() {
        atStateClaimNotifiedWithMinimumData();
        claimDetailsNotificationDate = LocalDateTime.now();
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateAwaitingCaseDetailsNotificationWithMinimalData() {
        atStateAwaitingCaseNotificationWithMinimalData();
        claimNotificationDate = LocalDateTime.now();
        claimDetailsNotificationDeadline = claimNotificationDate.plusDays(14);
        ccdState = AWAITING_CASE_DETAILS_NOTIFICATION;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateClaimCreatedWithMinimalData() {
        atStateAwaitingCaseDetailsNotificationWithMinimalData();

        claimDetailsNotificationDate = LocalDateTime.now();
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateRespondentRespondToClaimWithMinimalData(
        RespondentResponseType respondentResponseType
    ) {
        atStateServiceAcknowledgeWithMinimalData();
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateServiceAcknowledgeWithMinimalData() {
        atStateClaimCreatedWithMinimalData();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateProceedsOfflineUnrepresentedDefendantMinimumData() {
        atStatePendingClaimIssuedUnRepresentedDefendantMinimumData();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = null;
        respondentSolicitor1OrganisationDetails = null;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStatePendingClaimIssuedUnRepresentedDefendantMinimumData() {
        atStatePaymentSuccessfulMinimumData();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = NO;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStatePaymentSuccessfulMinimumData() {
        atStateClaimSubmittedMinimumData();
        claimIssuedPaymentDetails = new PaymentDetails()
            .setStatus(SUCCESS)
            .setReference("RC-1604-0739-2145-4711")
            ;
        paymentReference = "12345";
        paymentSuccessfulDate = LocalDateTime.now();
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateClaimSubmittedMinimumData() {
        atStateClaimDraftWithMinimalData();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = LocalDateTime.now();
        claimIssuedPaymentDetails = new PaymentDetails()
            .setCustomerReference("r")
            ;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateApplicantRespondToDefenceAndProceed() {
        atStateRespondentFullDefenceMinimumData();
        applicant1ProceedWithClaim = YES;
        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().documentName("claimant-response.pdf").build());
        applicant1DQ();
        applicant1ResponseDate = LocalDateTime.now();
        uiStatementOfTruth = new StatementOfTruth().setName("J").setRole("S");
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateApplicantRespondToDefenceAndNotProceedMinimumData() {
        atStateRespondentFullDefenceMinimumData();
        applicant1ProceedWithClaim = NO;
        applicant1ResponseDate = LocalDateTime.now();
        uiStatementOfTruth = new StatementOfTruth().setName("J").setRole("S");
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateRespondentFullDefenceMinimumData() {
        atStateRespondentRespondToClaimMinimumData(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().documentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateRespondentRespondToClaimMinimumData(
        RespondentResponseType respondentResponseType
    ) {
        atStateServiceAcknowledgeWithMinimalData();
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateNotificationAcknowledgedTimeExtensionMinimalData() {
        atStateServiceAcknowledgeWithMinimalData();
        respondent1TimeExtensionDate = LocalDateTime.now();
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }
}
