package uk.gov.hmcts.reform.civil.sampledata;

import joptsimple.internal.Strings;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.google.common.base.Strings.repeat;
import static java.math.BigDecimal.TEN;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseDataMaxEdgeCasesBuilder extends CaseDataBuilder {

    public static final int MAX_ALLOWED = 255;

    public static CaseDataMaxEdgeCasesBuilder builder() {
        return new CaseDataMaxEdgeCasesBuilder();
    }

    public CaseDataMaxEdgeCasesBuilder atStateClaimDraftWithMaximumData() {
        courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("127")
            .build();
        solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference(Strings.repeat('A', 24))
            .respondentSolicitor1Reference(Strings.repeat('R', 24))
            .build();
        applicant1 = PartyBuilder.builder().companyWithMaxData().build();
        applicant1LitigationFriendRequired = NO;
        applicantSolicitor1CheckEmail = CorrectEmail.builder()
            .email("hmcts.civil@gmail.com")
            .correct(YES)
            .build();
        applicant1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY").build())
            .build();
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY").build())
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
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.maximal().build();
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
        claimIssuedPaymentDetails = PaymentDetails.builder()
            .customerReference(repeat("1", MAX_ALLOWED))
            .build();
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
        claimIssuedPaymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .reference("RC-1604-0739-2145-4711")
            .build();
        paymentReference = "12345";
        paymentSuccessfulDate = LocalDateTime.now();
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStateProceedsOfflineUnregisteredDefendantMaximumData() {
        atStatePendingClaimIssuedUnRegisteredDefendantMaximumData();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = null;

        respondentSolicitor1OrganisationDetails = SolicitorOrganisationDetails.builder()
            .email("testorg@email.com")
            .organisationName(repeat("o", MAX_ALLOWED))
            .fax("123123123")
            .dx(repeat("d", AddressBuilder.MAX_ALLOWED))
            .phoneNumber("0123456789")
            .address(AddressBuilder.maximal().build())
            .build();
        return this;
    }

    public CaseDataMaxEdgeCasesBuilder atStatePendingClaimIssuedUnRegisteredDefendantMaximumData() {
        atStatePaymentSuccessfulMaximumData();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }
}
