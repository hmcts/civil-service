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

import java.math.BigDecimal;
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

        respondentSolicitor1OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg@email.com")
            .organisationName("test org name")
            .fax("123123123")
            .dx("test org dx")
            .phoneNumber("0123456789")
            .address(AddressBuilder.defaults().build())
            .build();
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateClaimDraftWithMinimalData() {
        courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
        applicant1 = PartyBuilder.builder().companyWithMinimalData().build();
        applicant1LitigationFriendRequired = NO;
        applicantSolicitor1CheckEmail = CorrectEmail.builder()
            .email("hmcts.civil@gmail.com")
            .correct(YES)
            .build();
        applicant1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY A").build())
            .build();
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY R").build())
            .build();
        respondent1 = PartyBuilder.builder().companyWithMinimalData().build();
        respondent1Represented = NO;
        claimType = ClaimType.CLINICAL_NEGLIGENCE;
        claimValue = ClaimValue.builder()
            .statementOfValueInPennies(BigDecimal.valueOf(10000000))
            .build();
        claimFee = Fee.builder()
            .calculatedAmountInPence(TEN)
            .code("fee code")
            .version("version 1")
            .build();
        paymentReference = "some reference";
        respondentSolicitor1EmailAddress = "respondentsolicitor@example.com";
        applicantSolicitor1UserDetails = IdamUserDetails.builder().email("applicantsolicitor@example.com").build();
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.minimal().build();
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

    public CaseDataMinEdgeCasesBuilder atStatePaymentSuccessfulWithMinimalData() {
        atStatePendingCaseIssuedWithMinimalData();
        claimIssuedPaymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .reference("RC-1604-0739-2145-4711")
            .build();
        paymentSuccessfulDate = LocalDateTime.now();
        claimDetailsNotificationDeadline = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateAwaitingCaseNotificationWithMinimalData() {
        atStatePaymentSuccessfulWithMinimalData();
        ccdState = CASE_ISSUED;
        issueDate = CLAIM_ISSUED_DATE;
        claimNotificationDeadline = LocalDateTime.now();
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
        claimIssuedPaymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .reference("RC-1604-0739-2145-4711")
            .build();
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
        claimIssuedPaymentDetails = PaymentDetails.builder()
            .customerReference("r")
            .build();
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateApplicantRespondToDefenceAndProceed() {
        atStateRespondentFullDefenceMinimumData();
        applicant1ProceedWithClaim = YES;
        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response.pdf").build())
            .build();
        applicant1DQ();
        applicant1ResponseDate = LocalDateTime.now();
        uiStatementOfTruth = StatementOfTruth.builder().name("J").role("S").build();
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateApplicantRespondToDefenceAndNotProceedMinimumData() {
        atStateRespondentFullDefenceMinimumData();
        applicant1ProceedWithClaim = NO;
        applicant1ResponseDate = LocalDateTime.now();
        uiStatementOfTruth = StatementOfTruth.builder().name("J").role("S").build();
        return this;
    }

    public CaseDataMinEdgeCasesBuilder atStateRespondentFullDefenceMinimumData() {
        atStateRespondentRespondToClaimMinimumData(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
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
}
