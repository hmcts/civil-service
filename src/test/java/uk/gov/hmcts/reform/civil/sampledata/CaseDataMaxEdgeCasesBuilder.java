package uk.gov.hmcts.reform.civil.sampledata;

import joptsimple.internal.Strings;
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
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.google.common.base.Strings.repeat;
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

public class CaseDataMaxEdgeCasesBuilder extends CaseDataBuilder {

    public static final int MAX_ALLOWED = 255;

    public static CaseDataMaxEdgeCasesBuilder builder() {
        return new CaseDataMaxEdgeCasesBuilder();
    }

    public CaseDataMaxEdgeCasesBuilder atStateClaimDraftWithMaximumData() {
        courtLocation = new CourtLocation()
            .setApplicantPreferredCourt("127")
            .setApplicantPreferredCourtLocationList(
                DynamicList.builder().value(DynamicListElement.builder().label("sitename").build()).build())
            .setCaseLocation(new CaseLocationCivil()
                              .setRegion("4")
                              .setBaseLocation("000000")
                              );
        solicitorReferences = new SolicitorReferences()
            .setApplicantSolicitor1Reference(Strings.repeat('A', 24))
            .setRespondentSolicitor1Reference(Strings.repeat('R', 24))
            ;
        applicant1 = PartyBuilder.builder().companyWithMaxData().build();
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
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.maximal();
        submittedDate = LocalDateTime.now();
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateClaimSubmittedMaximumData() {
        atStateClaimDraftWithMaximumData();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = LocalDateTime.now();
        claimIssuedPaymentDetails = new PaymentDetails()
            .setCustomerReference(repeat("1", MAX_ALLOWED))
            ;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateProceedsOfflineUnrepresentedDefendantMaximumData() {
        atStatePendingClaimIssuedUnRepresentedDefendantMaximumData();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = null;
        respondentSolicitor1OrganisationDetails = null;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStatePendingClaimIssuedUnRepresentedDefendantMaximumData() {
        atStatePaymentSuccessfulMaximumData();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = NO;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStatePaymentSuccessfulMaximumData() {
        atStateClaimSubmittedMaximumData();
        claimIssuedPaymentDetails = new PaymentDetails()
            .setStatus(SUCCESS)
            .setReference("RC-1604-0739-2145-4711")
            ;
        paymentReference = "12345";
        paymentSuccessfulDate = LocalDateTime.now();
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateProceedsOfflineUnregisteredDefendantMaximumData() {
        atStatePendingClaimIssuedUnRegisteredDefendantMaximumData();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = null;

        respondentSolicitor1OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg@email.com")
            .setOrganisationName(repeat("o", MAX_ALLOWED))
            .setFax("123123123")
            .setDx(repeat("d", AddressBuilder.MAX_ALLOWED))
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.maximal().build())
            ;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStatePendingClaimIssuedUnRegisteredDefendantMaximumData() {
        atStatePaymentSuccessfulMaximumData();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateClaimIssuedMaximumData() {
        atStatePaymentSuccessfulMaximumData();
        issueDate = CLAIM_ISSUED_DATE;
        claimNotificationDeadline = NOTIFICATION_DEADLINE;
        ccdState = CASE_ISSUED;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateRespondentRespondToClaimWithMaximalData(
        RespondentResponseType respondentResponseType
    ) {
        atStateNotificationAcknowledgedWithMaximumData();
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateNotificationAcknowledgedWithMaximumData() {
        atStateClaimDetailsNotifiedWithMaximumData();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = LocalDateTime.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateClaimDetailsNotifiedWithMaximumData() {
        atStateClaimNotifiedWithMaximumData();
        claimDetailsNotificationDate = LocalDateTime.now();
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateClaimNotifiedWithMaximumData() {
        atStateClaimIssuedWihMaximumData();
        claimNotificationDate = LocalDate.now().atStartOfDay();
        claimDetailsNotificationDeadline = DEADLINE;
        ccdState = AWAITING_CASE_DETAILS_NOTIFICATION;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateClaimIssuedWihMaximumData() {
        atStatePendingClaimIssuedWithMaximumData();
        claimNotificationDeadline = NOTIFICATION_DEADLINE;
        ccdState = CASE_ISSUED;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStatePendingClaimIssuedWithMaximumData() {
        atStatePaymentSuccessfulWithMaximumData();
        issueDate = CLAIM_ISSUED_DATE;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStatePaymentSuccessfulWithMaximumData() {
        atStateClaimSubmittedMaximumData();
        claimIssuedPaymentDetails = new PaymentDetails()
            .setStatus(SUCCESS)
            .setReference("RC-1604-0739-2145-4711")
            ;
        paymentReference = "12345";
        paymentSuccessfulDate = LocalDateTime.now();
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateApplicantRespondToDefenceAndNotProceedMaximumData() {
        atStateRespondentFullDefenceMaximumData();
        applicant1ProceedWithClaim = NO;
        applicant1ResponseDate = LocalDateTime.now();
        uiStatementOfTruth = StatementOfTruthBuilder.maximal();
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateApplicantRespondToDefenceAndProceed() {
        atStateRespondentFullDefenceMaximumData();
        applicant1ProceedWithClaim = YES;
        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().documentName("claimant-response.pdf").build());
        applicant1DQ();
        applicant1ResponseDate = LocalDateTime.now();
        uiStatementOfTruth = StatementOfTruthBuilder.maximal();
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateRespondentFullDefenceMaximumData() {
        atStateRespondentRespondToClaimMaximumData(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().documentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateRespondentRespondToClaimMaximumData(
        RespondentResponseType respondentResponseType
    ) {
        atStateNotificationAcknowledgedWithMaximumData();
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateNotificationAcknowledgedTimeExtensionMaximumData() {
        atStateNotificationAcknowledgedWithMaximumData();
        respondent1TimeExtensionDate = LocalDateTime.now();
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }
}
