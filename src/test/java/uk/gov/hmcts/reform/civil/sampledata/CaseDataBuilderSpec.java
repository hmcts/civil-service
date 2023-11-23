package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseDataBuilderSpec {

    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final Long CASE_ID = 1594901956117591L;
    public static final LocalDateTime SUBMITTED_DATE_TIME = LocalDateTime.now();
    public static final LocalDateTime RESPONSE_DEADLINE = SUBMITTED_DATE_TIME.toLocalDate().plusDays(14)
        .atTime(23, 59, 59);
    public static final LocalDateTime NOTIFICATION_DEADLINE = LocalDate.now().atStartOfDay().plusDays(14);

    // Create Claim
    protected CaseCategory caseAccessCategory;
    protected Long ccdCaseReference;
    protected SolicitorReferences solicitorReferences;
    protected Party applicant1;
    protected Party applicant2;
    protected Party respondent1;
    protected Party respondent2;
    protected YesOrNo respondent1Represented;
    protected YesOrNo respondent2Represented;
    protected String respondentSolicitor1EmailAddress;
    protected String respondentSolicitor2EmailAddress;
    protected ClaimValue claimValue;
    protected DynamicList applicantSolicitor1PbaAccounts;
    protected Fee claimFee;
    protected StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    protected String legacyCaseReference;
    protected CaseState ccdState;
    protected PaymentDetails claimIssuedPaymentDetails;
    protected CorrectEmail applicantSolicitor1CheckEmail;
    protected IdamUserDetails applicantSolicitor1UserDetails;
    //Acknowledge Claim
    protected YesOrNo respondent1OrgRegistered;
    protected YesOrNo respondent2OrgRegistered;
    protected OrganisationPolicy applicant1OrganisationPolicy;
    protected OrganisationPolicy respondent1OrganisationPolicy;
    protected OrganisationPolicy respondent2OrganisationPolicy;
    protected YesOrNo addApplicant2;
    protected YesOrNo addRespondent2;
    protected YesOrNo respondent2SameLegalRepresentative;

    protected LocalDateTime respondent1ResponseDeadline;
    protected LocalDateTime respondent2ResponseDeadline;

    //Deadline extension
    protected LocalDate respondentSolicitor1AgreedDeadlineExtension;
    protected LocalDate respondentSolicitor2AgreedDeadlineExtension;
    protected LocalDateTime respondent1TimeExtensionDate;
    protected LocalDateTime respondent2TimeExtensionDate;
    protected LocalDateTime claimNotificationDeadline;

    //dates
    protected LocalDateTime submittedDate;
    protected LocalDate issueDate;
    protected LocalDateTime takenOfflineDate;

    private DefendantPinToPostLRspec respondent1PinToPostLRspec;

    private String respondent1OrganisationIDCopy;
    private String respondent2OrganisationIDCopy;

    //workaround fields
    protected Party respondent1Copy;
    protected Party respondent2Copy;

    public CaseDataBuilderSpec respondentSolicitor1EmailAddress(String respondentSolicitor1EmailAddress) {
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        return this;
    }

    public CaseDataBuilderSpec respondent2(Party party) {
        this.respondent2 = party;
        return this;
    }

    public CaseDataBuilderSpec claimValue(ClaimValue claimValue) {
        this.claimValue = claimValue;
        return this;
    }

    public CaseDataBuilderSpec applicant1(Party party) {
        this.applicant1 = party;
        return this;
    }

    public CaseDataBuilderSpec applicant2(Party party) {
        this.applicant2 = party;
        return this;
    }

    public CaseDataBuilderSpec respondent1(Party party) {
        this.respondent1 = party;
        return this;
    }

    public CaseDataBuilderSpec legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public CaseDataBuilderSpec respondent1Represented(YesOrNo isRepresented) {
        this.respondent1Represented = isRepresented;
        return this;
    }

    public CaseDataBuilderSpec respondent2Represented(YesOrNo isRepresented) {
        this.respondent2Represented = isRepresented;
        return this;
    }

    public CaseDataBuilderSpec respondent1OrgRegistered(YesOrNo respondent1OrgRegistered) {
        this.respondent1OrgRegistered = respondent1OrgRegistered;
        return this;
    }

    public CaseDataBuilderSpec respondent2OrgRegistered(YesOrNo respondent2OrgRegistered) {
        this.respondent2OrgRegistered = respondent2OrgRegistered;
        return this;
    }

    public CaseDataBuilderSpec applicant1OrganisationPolicy(OrganisationPolicy applicant1OrganisationPolicy) {
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderSpec respondent1OrganisationPolicy(OrganisationPolicy respondent1OrganisationPolicy) {
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderSpec respondent2OrganisationPolicy(OrganisationPolicy respondent2OrganisationPolicy) {
        this.respondent2OrganisationPolicy = respondent2OrganisationPolicy;
        return this;
    }

    public CaseDataBuilderSpec addRespondent2(YesOrNo addRespondent2) {
        this.addRespondent2 = addRespondent2;
        return this;
    }

    public CaseDataBuilderSpec respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        return this;
    }

    public CaseDataBuilderSpec caseReference(Long ccdCaseReference) {
        this.ccdCaseReference = ccdCaseReference;
        return this;
    }

    public CaseDataBuilderSpec respondent1ResponseDeadline(LocalDateTime deadline) {
        this.respondent1ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilderSpec respondent2ResponseDeadline(LocalDateTime deadline) {
        this.respondent2ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilderSpec respondent1TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent1TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec respondent2TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent2TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor1AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec respondentSolicitor2AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor2AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilderSpec claimNotificationDeadline(LocalDateTime deadline) {
        this.claimNotificationDeadline = deadline;
        return this;
    }

    public CaseDataBuilderSpec takenOfflineDate(LocalDateTime takenOfflineDate) {
        this.takenOfflineDate = takenOfflineDate;
        return this;
    }

    public CaseDataBuilderSpec addRespondent1PinToPostLRspec(DefendantPinToPostLRspec respondent1PinToPostLRspec) {
        this.respondent1PinToPostLRspec = respondent1PinToPostLRspec;
        return this;
    }

    public CaseDataBuilderSpec atState(FlowState.Main flowState) {
        return atState(flowState, ONE_V_ONE);
    }

    public CaseDataBuilderSpec atState(FlowState.Main flowState, MultiPartyScenario mpScenario) {
        switch (flowState) {
            case SPEC_DRAFT:
                return atStateClaimDraft();
            case CLAIM_SUBMITTED:
                return atStateSpec1v1ClaimSubmitted();
            case CLAIM_ISSUED_PAYMENT_SUCCESSFUL:
                return atStateSpec1v1PaymentSuccessful(true);
            default:
                throw new IllegalArgumentException("Invalid internal state: " + flowState);
        }
    }

    public CaseDataBuilderSpec atStateClaimDraft() {
        caseAccessCategory = CaseCategory.SPEC_CLAIM;
        solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference("12345")
            .respondentSolicitor1Reference("6789")
            .build();
        claimValue = ClaimValue.builder()
            .statementOfValueInPennies(BigDecimal.valueOf(10000000))
            .build();
        applicantSolicitor1PbaAccounts = DynamicList.builder()
            .value(DynamicListElement.builder().label("PBA0077597").build())
            .build();
        claimFee = Fee.builder()
            .version("1")
            .code("CODE")
            .calculatedAmountInPence(BigDecimal.valueOf(100))
            .build();
        applicant1 = PartyBuilder.builder().individual().build();
        respondent1 = PartyBuilder.builder().soleTrader().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2OrgRegistered = YES;
        applicant1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY A").build())
            .build();
        respondent1OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY R").build())
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
            .build();
        respondent2OrganisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("QWERTY R2").build())
            .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]")
            .build();
        respondent1OrganisationIDCopy = respondent1OrganisationPolicy.getOrganisation().getOrganisationID();
        respondent2OrganisationIDCopy = respondent2OrganisationPolicy.getOrganisation().getOrganisationID();
        respondentSolicitor1EmailAddress = "respondentsolicitor@example.com";
        respondentSolicitor2EmailAddress = "respondentsolicitor2@example.com";
        applicantSolicitor1UserDetails = IdamUserDetails.builder().email("applicantsolicitor@example.com").build();
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.defaults().build();
        applicantSolicitor1CheckEmail = CorrectEmail.builder().email("hmcts.civil@gmail.com").correct(YES).build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1ClaimSubmitted() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        claimIssuedPaymentDetails = PaymentDetails.builder().customerReference("12345").build();
        return this;
    }

    public CaseDataBuilderSpec atStateClaimSubmittedTwoRespondentSameSolicitorSpec() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilderSpec atStateClaimSubmittedTwoRespondentDifferentSolicitorSpec() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateClaimSubmitted2v1() {
        atStateSpec1v1ClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1DefendantUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        respondent1Represented = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec2v1DefendantUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2OneDefendantUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2Represented = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2BothDefendantUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;
        respondent2Represented = NO;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1DefendantUnregisteredClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec2v1DefendantUnregisteredClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2Solicitor1UnregisteredSolicitor2RegisteredAndRepresented() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;
        respondent1OrgRegistered = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2DifferentSolicitorBothDefendantRepresentedAndUnregistered() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = YES;
        respondent2Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2SameSolicitorBothDefendantRepresentedAndUnregistered() {
        atStateSpec1v1ClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = YES;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1PaymentSuccessful(Boolean respondentRepresented) {
        if (respondentRepresented) {
            atStateSpec1v1ClaimSubmitted();
        } else {
            atStateSpec1v1DefendantUnrepresentedClaimSubmitted();
        }

        ccdState = CASE_ISSUED;
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS)
                                                            .customerReference("12345")
                                                            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1PaymentSuccessful() {
        atStateSpec1v1ClaimSubmitted();
        ccdState = CASE_ISSUED;
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS)
                                                            .customerReference("12345")
                                                            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateClaim1v2SameSolicitorTimeExtension() {
        atStateClaimSubmittedTwoRespondentSameSolicitorSpec();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        respondent1TimeExtensionDate = submittedDate.plusDays(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1PaymentFailed() {
        atStateSpec1v1ClaimSubmitted();
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.FAILED)
                                                            .customerReference("12345")
                                                            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2SameSolicitorBothDefendantRepresentedPaymentSuccessful() {
        atStateClaimSubmittedTwoRespondentSameSolicitorSpec();
        ccdState = CASE_ISSUED;
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS)
            .customerReference("12345")
            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2SameSolicitorBothDefendantUnrepresentedPaymentSuccessful() {
        atStateSpec1v2BothDefendantUnrepresentedClaimSubmitted();
        ccdState = CASE_ISSUED;
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS)
            .customerReference("12345")
            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2DifferentSolicitorBothDefendantRepresentedPaymentFailed() {
        atStateClaimSubmittedTwoRespondentDifferentSolicitorSpec();
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.FAILED)
            .customerReference("12345")
            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2DifferentSolicitorOneDefendantUnrepresentedPaymentSuccessful() {
        atStateSpec1v2BothDefendantUnrepresentedClaimSubmitted();
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS)
            .customerReference("12345")
            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2SameSolicitorBothDefendantUnregisteredPaymentSuccessful() {
        atStateSpec1v2SameSolicitorBothDefendantRepresentedAndUnregistered();
        ccdState = CASE_ISSUED;
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS)
            .customerReference("12345")
            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2DifferentSolicitorBothDefendantUnregisteredPaymentSuccessful() {
        atStateSpec1v2DifferentSolicitorBothDefendantRepresentedAndUnregistered();
        ccdState = CASE_ISSUED;
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS)
            .customerReference("12345")
            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2OneDefendantUnregisteredOtherUnrepresentedPaymentSuccessful() {
        atStateSpec1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted();
        ccdState = CASE_ISSUED;
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS)
            .customerReference("12345")
            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec2v1PaymentFailure() {
        atStateClaimSubmitted2v1();
        claimIssuedPaymentDetails = PaymentDetails.builder().status(PaymentStatus.FAILED)
            .customerReference("12345")
            .build();
        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1RepresentedPendingClaimIssued() {
        atStateSpec1v1PaymentSuccessful(true);
        issueDate = LocalDate.now();

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v1UnrepresentedPendingClaimIssued() {
        atStateSpec1v1PaymentSuccessful(false);
        issueDate = LocalDate.now();

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2SameSolicitorBothUnrepresentedPendingClaimIssued() {
        atStateSpec1v2SameSolicitorBothDefendantUnrepresentedPaymentSuccessful();
        issueDate = LocalDate.now();

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2SameSolicitorBothUnregisteredPendingClaimIssued() {
        atStateSpec1v2SameSolicitorBothDefendantUnregisteredPaymentSuccessful();
        issueDate = LocalDate.now();

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2DifferentSolicitorBothUnregisteredPendingClaimIssued() {
        atStateSpec1v2DifferentSolicitorBothDefendantUnregisteredPaymentSuccessful();
        issueDate = LocalDate.now();

        return this;
    }

    public CaseDataBuilderSpec atStateSpec1v2OneDefendantUnregisteredOtherUnrepresentedPendingClaimIssued() {
        atStateSpec1v2OneDefendantUnregisteredOtherUnrepresentedPaymentSuccessful();
        issueDate = LocalDate.now();

        return this;
    }

    public CaseDataBuilderSpec atStateSpecClaimIssued() {
        atStateSpec1v1RepresentedPendingClaimIssued();
        claimNotificationDeadline = NOTIFICATION_DEADLINE;
        return this;
    }

    public CaseDataBuilderSpec atStateClaimIssuedFromPendingClaimIssuedUnrepresentedDefendant1v1Spec() {
        atStateSpec1v1UnrepresentedPendingClaimIssued();
        respondent1Represented = NO;
        claimNotificationDeadline = LocalDate.now().atStartOfDay().plusDays(14);
        respondent1PinToPostLRspec = DefendantPinToPostLRspec.builder()
                                           .expiryDate(LocalDate.now())
                                           .citizenCaseRole("citizen")
                                           .respondentCaseRole("respondent")
                                           .accessCode("123").build();

        return this;
    }

    public CaseDataBuilderSpec atStateTakenOfflineUnrepresentedDefendantSameSolicitor() {
        atStateSpec1v2SameSolicitorBothUnrepresentedPendingClaimIssued();
        issueDate = LocalDate.now();
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderSpec atStateTakenOfflineOneUnregisteredDefendantDifferentSolicitor() {
        atStateSpec1v2DifferentSolicitorBothUnregisteredPendingClaimIssued();
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        issueDate = LocalDate.now();
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilderSpec atStateTakenOfflineOneDefendantUnregisteredOtherUnrepresented() {
        atStateSpec1v2OneDefendantUnregisteredOtherUnrepresentedPendingClaimIssued();
        issueDate = LocalDate.now();
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public static CaseDataBuilderSpec builder() {
        return new CaseDataBuilderSpec();
    }

    public CaseData build() {
        return CaseData.builder()
            // Create Claim
            .caseAccessCategory(caseAccessCategory)
            .legacyCaseReference(legacyCaseReference)
            .solicitorReferences(solicitorReferences)
            .claimValue(claimValue)
            .applicantSolicitor1PbaAccounts(applicantSolicitor1PbaAccounts)
            .claimFee(claimFee)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent1Represented(respondent1Represented)
            .respondent2Represented(respondent2Represented)
            .respondent1OrgRegistered(respondent1OrgRegistered)
            .respondent2OrgRegistered(respondent2OrgRegistered)
            .respondentSolicitor1EmailAddress(respondentSolicitor1EmailAddress)
            .respondentSolicitor2EmailAddress(respondentSolicitor2EmailAddress)
            .applicantSolicitor1ClaimStatementOfTruth(applicantSolicitor1ClaimStatementOfTruth)
            .claimIssuedPaymentDetails(claimIssuedPaymentDetails)
            .claimFee(claimFee)
            .applicantSolicitor1CheckEmail(applicantSolicitor1CheckEmail)
            .applicantSolicitor1UserDetails(applicantSolicitor1UserDetails)
            .ccdState(ccdState)
            .ccdCaseReference(ccdCaseReference)
            .applicant1OrganisationPolicy(applicant1OrganisationPolicy)
            .respondent1OrganisationPolicy(respondent1OrganisationPolicy)
            .respondent2OrganisationPolicy(respondent2OrganisationPolicy)
            .addApplicant2(addApplicant2)
            .addRespondent2(addRespondent2)
            .respondent2SameLegalRepresentative(respondent2SameLegalRepresentative)
            //dates
            .submittedDate(submittedDate)
            .issueDate(issueDate)
            .claimNotificationDeadline(claimNotificationDeadline)
            .takenOfflineDate(takenOfflineDate)
            //workaround fields
            .respondent1Copy(respondent1Copy)
            .respondent2Copy(respondent2Copy)
            .respondent1OrganisationIDCopy(respondent1OrganisationIDCopy)
            .respondent2OrganisationIDCopy(respondent2OrganisationIDCopy)
            .respondent1PinToPostLRspec(respondent1PinToPostLRspec)
            .build();
    }

}
