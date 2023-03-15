package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
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

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.PersonalInjuryType.ROAD_ACCIDENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseDataBuilderUnspec {

    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final Long CASE_ID = 1594901956117591L;
    public static final LocalDateTime SUBMITTED_DATE_TIME = LocalDateTime.now();
    public static final LocalDateTime RESPONSE_DEADLINE = SUBMITTED_DATE_TIME.toLocalDate().plusDays(14)
        .atTime(23, 59, 59);

    // Create Claim
    protected Long ccdCaseReference;
    protected CaseCategory caseAccessCategory;
    protected SolicitorReferences solicitorReferences;
    protected String respondentSolicitor2Reference;
    protected CourtLocation courtLocation;
    protected Party applicant1;
    protected Party applicant2;
    protected Party respondent1;
    protected Party respondent2;
    protected YesOrNo respondent1Represented;
    protected YesOrNo respondent2Represented;
    protected String respondentSolicitor1EmailAddress;
    protected String respondentSolicitor2EmailAddress;
    protected ClaimValue claimValue;
    protected ClaimType claimType;
    protected PersonalInjuryType personalInjuryType;
    protected DynamicList applicantSolicitor1PbaAccounts;
    protected Fee claimFee;
    protected StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    protected StatementOfTruth uiStatementOfTruth;
    protected String legacyCaseReference;
    protected AllocatedTrack allocatedTrack;
    protected String responseClaimTrack;
    protected CaseState ccdState;
    protected PaymentDetails claimIssuedPaymentDetails;
    protected CorrectEmail applicantSolicitor1CheckEmail;
    protected IdamUserDetails applicantSolicitor1UserDetails;
    //Acknowledge Claim
    protected ResponseIntention respondent1ClaimResponseIntentionType;
    protected ResponseIntention respondent2ClaimResponseIntentionType;
    protected YesOrNo respondentResponseIsSame;

    protected BusinessProcess businessProcess;

    protected YesOrNo respondent1OrgRegistered;
    protected YesOrNo respondent2OrgRegistered;
    protected OrganisationPolicy applicant1OrganisationPolicy;
    protected OrganisationPolicy respondent1OrganisationPolicy;
    protected OrganisationPolicy respondent2OrganisationPolicy;
    protected YesOrNo addApplicant2;
    protected YesOrNo addRespondent2;
    protected YesOrNo respondent2SameLegalRepresentative;
    //dates
    protected LocalDateTime submittedDate;
    protected LocalDate issueDate;

    private String respondent1OrganisationIDCopy;
    private String respondent2OrganisationIDCopy;
    //workaround fields
    protected Party respondent1Copy;
    protected Party respondent2Copy;

    public CaseDataBuilderUnspec respondentSolicitor1EmailAddress(String respondentSolicitor1EmailAddress) {
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        return this;
    }

    public CaseDataBuilderUnspec respondentSolicitor2EmailAddress(String respondentSolicitor2EmailAddress) {
        this.respondentSolicitor2EmailAddress = respondentSolicitor2EmailAddress;
        return this;
    }

    public CaseDataBuilderUnspec respondent2(Party party) {
        this.respondent2 = party;
        return this;
    }

    public CaseDataBuilderUnspec claimValue(ClaimValue claimValue) {
        this.claimValue = claimValue;
        return this;
    }

    public CaseDataBuilderUnspec applicant1(Party party) {
        this.applicant1 = party;
        return this;
    }

    public CaseDataBuilderUnspec applicant2(Party party) {
        this.applicant2 = party;
        return this;
    }

    public CaseDataBuilderUnspec respondent1(Party party) {
        this.respondent1 = party;
        return this;
    }

    public CaseDataBuilderUnspec legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public CaseDataBuilderUnspec respondent1Represented(YesOrNo isRepresented) {
        this.respondent1Represented = isRepresented;
        return this;
    }

    public CaseDataBuilderUnspec respondent2Represented(YesOrNo isRepresented) {
        this.respondent2Represented = isRepresented;
        return this;
    }

    public CaseDataBuilderUnspec respondent1OrgRegistered(YesOrNo respondent1OrgRegistered) {
        this.respondent1OrgRegistered = respondent1OrgRegistered;
        return this;
    }

    public CaseDataBuilderUnspec respondent2OrgRegistered(YesOrNo respondent2OrgRegistered) {
        this.respondent2OrgRegistered = respondent2OrgRegistered;
        return this;
    }

    public CaseDataBuilderUnspec addRespondent2(YesOrNo addRespondent2) {
        this.addRespondent2 = addRespondent2;
        return this;
    }

    public CaseDataBuilderUnspec respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        return this;
    }

    public CaseDataBuilderUnspec caseReference(Long ccdCaseReference) {
        this.ccdCaseReference = ccdCaseReference;
        return this;
    }

    public CaseDataBuilderUnspec atState(FlowState.Main flowState) {
        return atState(flowState, ONE_V_ONE);
    }

    public CaseDataBuilderUnspec atState(FlowState.Main flowState, MultiPartyScenario mpScenario) {
        switch (flowState) {
            case DRAFT:
                return atStateClaimDraft();
            case CLAIM_SUBMITTED:
                return atStateClaimSubmitted();
            default:
                throw new IllegalArgumentException("Invalid internal state: " + flowState);
        }
    }

    public CaseDataBuilderUnspec atStateClaimDraft() {
        caseAccessCategory = CaseCategory.UNSPEC_CLAIM;
        solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference("12345")
            .respondentSolicitor1Reference("6789")
            .build();
        courtLocation = CourtLocation.builder()
            .applicantPreferredCourtLocationList(
                DynamicList.builder().value(DynamicListElement.builder().label("sitename").build()).build())
            .build();
        claimValue = ClaimValue.builder()
            .statementOfValueInPennies(BigDecimal.valueOf(10000000))
            .build();
        claimType = ClaimType.PERSONAL_INJURY;
        personalInjuryType = ROAD_ACCIDENT;
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

    public CaseDataBuilderUnspec atStateClaimSubmitted() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        claimIssuedPaymentDetails = PaymentDetails.builder().customerReference("12345").build();
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmittedTwoRespondentSameSolicitor() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmittedTwoRespondentRepresentatives() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered() {
        atStateClaimSubmitted();
        multiPartyClaimTwoDefendantSolicitors();
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atStateClaimSubmitted2v1() {
        atStateClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        return this;
    }

    public CaseDataBuilderUnspec atState1v1DefendantUnrepresentedClaimSubmitted() {
        atStateClaimSubmitted();
        respondent1Represented = NO;
        return this;
    }

    public CaseDataBuilderUnspec atState2v1DefendantUnrepresentedClaimSubmitted() {
        atStateClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;
        return this;
    }

    public CaseDataBuilderUnspec atState1v2OneDefendantUnrepresentedClaimSubmitted() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2Represented = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atState1v2BothDefendantUnrepresentedClaimSubmitted() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;
        respondent2Represented = NO;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atState1v1DefendantUnregisteredClaimSubmitted() {
        atStateClaimSubmitted();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atState2v1DefendantUnregisteredClaimSubmitted() {
        atStateClaimSubmitted();
        addApplicant2 = YES;
        applicant2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec atState1v2Solicitor1UnregisteredSolicitor2Registered() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilderUnspec atState1v2OneDefendantRepresentedUnregisteredOtherUnrepresentedClaimSubmitted() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent1Represented = NO;
        respondent1OrgRegistered = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        return this;
    }

    public CaseDataBuilderUnspec atState1v2BothDefendantRepresentedAndUnregistered() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build();
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = YES;
        respondent2Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilderUnspec applicantSolicitor1UserDetails(IdamUserDetails applicantSolicitor1UserDetails) {
        this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
        return this;
    }

    public CaseDataBuilderUnspec multiPartyClaimTwoDefendantSolicitors() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        return this;
    }

    public CaseDataBuilderUnspec multiPartyClaimOneDefendantSolicitor() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilderUnspec multiPartyClaimTwoApplicants() {
        this.addApplicant2 = YES;
        this.applicant2 = PartyBuilder.builder().individual("Jason").build();
        return this;
    }

    public CaseDataBuilderUnspec respondent1Copy(Party party) {
        this.respondent1Copy = party;
        return this;
    }

    public CaseDataBuilderUnspec respondent2Copy(Party party) {
        this.respondent2Copy = party;
        return this;
    }

    public static CaseDataBuilderUnspec builder() {
        return new CaseDataBuilderUnspec();
    }

    public CaseData build() {

        return CaseData.builder()
            // Create Claim
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
            //workaround fields
            .respondent1Copy(respondent1Copy)
            .respondent2Copy(respondent2Copy)
            .respondent1OrganisationIDCopy(respondent1OrganisationIDCopy)
            .respondent2OrganisationIDCopy(respondent2OrganisationIDCopy)
            .build();
    }
}
