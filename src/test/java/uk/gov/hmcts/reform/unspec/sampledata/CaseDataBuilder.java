package uk.gov.hmcts.reform.unspec.sampledata;

import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;
import uk.gov.hmcts.reform.unspec.enums.CaseState;
import uk.gov.hmcts.reform.unspec.enums.ClaimType;
import uk.gov.hmcts.reform.unspec.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.unspec.enums.RespondentResponseType;
import uk.gov.hmcts.reform.unspec.enums.ResponseIntention;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;
import uk.gov.hmcts.reform.unspec.model.CloseClaim;
import uk.gov.hmcts.reform.unspec.model.CourtLocation;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.PaymentDetails;
import uk.gov.hmcts.reform.unspec.model.ResponseDocument;
import uk.gov.hmcts.reform.unspec.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.unspec.model.ServiceLocation;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.AWAITING_CLAIMANT_INTENTION;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.CLOSED;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.CREATED;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.PROCEEDS_WITH_OFFLINE_JOURNEY;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.STAYED;
import static uk.gov.hmcts.reform.unspec.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.unspec.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.unspec.enums.PersonalInjuryType.ROAD_ACCIDENT;
import static uk.gov.hmcts.reform.unspec.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.CLAIM_FORM;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.OTHER;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.ServiceLocationType.BUSINESS;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;

public class CaseDataBuilder {

    public static final String LEGACY_CASE_REFERENCE = "000LR001";
    public static final Long CASE_ID = 1594901956117591L;
    public static final LocalDate DEEMED_SERVICE_DATE = LocalDate.now();
    public static final LocalDateTime RESPONSE_DEADLINE = now().plusDays(14).atTime(23, 59, 59);
    public static final LocalDateTime APPLICANT_RESPONSE_DEADLINE = LocalDateTime.now().plusDays(120);

    // Create Claim
    private Long ccdCaseReference;
    private SolicitorReferences solicitorReferences;
    private CourtLocation courtLocation;
    private Party applicant1;
    private Party respondent1;
    private ClaimValue claimValue;
    private ClaimType claimType;
    private String claimTypeOther;
    private PersonalInjuryType personalInjuryType;
    private String personalInjuryTypeOther;
    private StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    private LocalDateTime claimSubmittedDateTime;
    private LocalDate claimIssuedDate;
    private String legacyCaseReference;
    private LocalDateTime confirmationOfServiceDeadline;
    private AllocatedTrack allocatedTrack;
    private CaseState ccdState;
    private List<Element<CaseDocument>> systemGeneratedCaseDocuments;
    private PaymentDetails paymentDetails;
    // Confirm Service
    private LocalDate deemedServiceDateToRespondentSolicitor1;
    private LocalDateTime respondentSolicitor1ResponseDeadline;
    private ServiceMethod serviceMethodToRespondentSolicitor1;
    private ServiceLocation serviceLocationToRespondentSolicitor1;
    private List<ServedDocuments> servedDocuments;
    private String servedDocumentsOther;
    private ServedDocumentFiles servedDocumentFiles;
    private LocalDate serviceDateToRespondentSolicitor1;
    private LocalDateTime serviceDateTimeToRespondentSolicitor1;
    private StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1;
    private String serviceNamedPersonToRespondentSolicitor1;
    //Acknowledge Service
    private ResponseIntention respondent1ClaimResponseIntentionType;
    // Request Extension
    private LocalDate respondentSolicitor1claimResponseExtensionProposedDeadline;
    private YesOrNo respondentSolicitor1claimResponseExtensionAlreadyAgreed;
    private String respondentSolicitor1claimResponseExtensionReason;
    // Respond To Extension Request
    private YesOrNo respondentSolicitor1claimResponseExtensionAccepted;
    private YesOrNo respondentSolicitor1claimResponseExtensionCounter;
    private LocalDate respondentSolicitor1claimResponseExtensionCounterDate;
    private String respondentSolicitor1claimResponseExtensionRejectionReason;
    // Defendant Response
    private RespondentResponseType respondent1ClaimResponseType;
    private ResponseDocument respondent1ClaimResponseDocument;
    private LocalDateTime applicantSolicitorResponseDeadlineToRespondentSolicitor1;
    // Claimant Response
    private YesOrNo applicant1ProceedWithClaim;
    private ResponseDocument applicant1DefenceResponseDocument;
    private BusinessProcess businessProcess;

    private CloseClaim withdrawClaim;
    private CloseClaim discontinueClaim;

    private Respondent1DQ respondent1DQ;

    public CaseDataBuilder respondentSolicitor1claimResponseExtensionProposedDeadline(LocalDate responseDeadline) {
        this.respondentSolicitor1claimResponseExtensionProposedDeadline = responseDeadline;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1ResponseDeadline(LocalDateTime respondentSolicitor1ResponseDeadline) {
        this.respondentSolicitor1ResponseDeadline = respondentSolicitor1ResponseDeadline;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1claimResponseExtensionAccepted(YesOrNo yesOrNo) {
        this.respondentSolicitor1claimResponseExtensionAccepted = yesOrNo;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1claimResponseExtensionCounter(YesOrNo yesOrNo) {
        this.respondentSolicitor1claimResponseExtensionCounter = yesOrNo;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1claimResponseExtensionAlreadyAgreed(YesOrNo yesOrNo) {
        this.respondentSolicitor1claimResponseExtensionAlreadyAgreed = yesOrNo;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1claimResponseExtensionCounterDate(LocalDate date) {
        this.respondentSolicitor1claimResponseExtensionCounterDate = date;
        return this;
    }

    public CaseDataBuilder respondent1DQ(Respondent1DQ respondent1DQ) {
        this.respondent1DQ = respondent1DQ;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaim(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaim = yesOrNo;
        return this;
    }

    public CaseDataBuilder disccontinueClaim(CloseClaim closeClaim) {
        this.discontinueClaim = closeClaim;
        return this;
    }

    public CaseDataBuilder claimValue(ClaimValue claimValue) {
        this.claimValue = claimValue;
        return this;
    }

    public CaseDataBuilder claimIssuedDate(LocalDate claimIssuedDate) {
        this.claimIssuedDate = claimIssuedDate;
        return this;
    }

    public CaseDataBuilder serviceDateToRespondentSolicitor1(LocalDate serviceDateToRespondentSolicitor1) {
        this.serviceDateToRespondentSolicitor1 = serviceDateToRespondentSolicitor1;
        return this;
    }

    public CaseDataBuilder serviceDateTimeToRespondentSolicitor1(LocalDateTime serviceDateTimeToRespondentSolicitor1) {
        this.serviceDateTimeToRespondentSolicitor1 = serviceDateTimeToRespondentSolicitor1;
        return this;
    }

    public CaseDataBuilder systemGeneratedCaseDocuments(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
        this.systemGeneratedCaseDocuments = systemGeneratedCaseDocuments;
        return this;
    }

    public CaseDataBuilder applicant1(Party party) {
        this.applicant1 = party;
        return this;
    }

    public CaseDataBuilder serviceMethodToRespondentSolicitor1(ServiceMethod serviceMethodToRespondentSolicitor1) {
        this.serviceMethodToRespondentSolicitor1 = serviceMethodToRespondentSolicitor1;
        return this;
    }

    public CaseDataBuilder respondent1(Party party) {
        this.respondent1 = party;
        return this;
    }

    public CaseDataBuilder servedDocumentsOther(String servedDocumentsOther) {
        this.servedDocumentsOther = servedDocumentsOther;
        return this;
    }

    public CaseDataBuilder legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public CaseDataBuilder atState(FlowState.Main flowState) {
        switch (flowState) {
            case DRAFT:
                return atStateClaimDraft();
            case PENDING_CASE_ISSUED:
                return atStatePendingCaseIssued();
            case PAYMENT_SUCCESSFUL:
                return atStatePaymentSuccessful();
            case PAYMENT_FAILED:
                return atStatePaymentFailed();
            case CLAIM_ISSUED:
                return atStateClaimCreated();
            case CLAIM_STAYED:
                return atStateClaimStayed();
            case SERVICE_CONFIRMED:
                return atStateServiceConfirmed();
            case SERVICE_ACKNOWLEDGED:
                return atStateServiceAcknowledge();
            case EXTENSION_REQUESTED:
                return atStateExtensionRequested();
            case EXTENSION_RESPONDED:
                return atStateExtensionResponded();
            case RESPONDED_TO_CLAIM:
                return atStateRespondedToClaim();
            case FULL_DEFENCE:
                return atStateFullDefence();
            case CLAIM_WITHDRAWN:
                return atStateClaimWithdrawn();
            case CLAIM_DISCONTINUED:
                return atStateClaimDiscontinued();
            case PROCEEDS_WITH_OFFLINE_JOURNEY:
                return atStateProceedsOffline();
            default:
                throw new IllegalArgumentException("Invalid internal state: " + flowState);
        }
    }

    public CaseDataBuilder atStateClaimDiscontinued() {
        atStateClaimCreated();
        return discontinueClaim();
    }

    public CaseDataBuilder discontinueClaim() {
        this.ccdState = CLOSED;
        this.discontinueClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilder discontinueClaimFrom(FlowState.Main flowState) {
        atState(flowState);
        this.ccdState = CLOSED;
        this.discontinueClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimWithdrawn() {
        atStateClaimCreated();
        return withdrawClaim();
    }

    public CaseDataBuilder withdrawClaim(CloseClaim closeClaim) {
        this.withdrawClaim = closeClaim;
        return this;
    }

    public CaseDataBuilder withdrawClaim() {
        this.ccdState = CLOSED;
        this.withdrawClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilder withdrawClaimFrom(FlowState.Main flowState) {
        atState(flowState);
        this.ccdState = CLOSED;
        this.withdrawClaim = CloseClaim.builder()
            .date(LocalDate.now())
            .reason("My reason")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimDraft() {
        solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference("12345")
            .respondentSolicitor1Reference("6789")
            .build();
        courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("The court location")
            .build();
        claimValue = ClaimValue.builder()
            .statementOfValueInPennies(BigDecimal.valueOf(10000000))
            .build();
        claimType = ClaimType.PERSONAL_INJURY;
        personalInjuryType = ROAD_ACCIDENT;
        applicant1 = PartyBuilder.builder().individual().build();
        respondent1 = PartyBuilder.builder().soleTrader().build();
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.builder().build();

        return this;
    }

    public CaseDataBuilder atStatePendingCaseIssued() {
        atStateClaimDraft();
        claimSubmittedDateTime = LocalDateTime.now();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        return this;
    }

    public CaseDataBuilder atStatePaymentFailed() {
        atStatePendingCaseIssued();

        paymentDetails = PaymentDetails.builder()
            .status(FAILED)
            .errorMessage("Your account is deleted")
            .errorCode("CA-E0004")
            .build();
        return this;
    }

    public CaseDataBuilder atStatePaymentSuccessful() {
        atStatePendingCaseIssued();
        paymentDetails = PaymentDetails.builder()
            .status(SUCCESS)
            .reference("RC-1604-0739-2145-4711")
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimCreated() {
        atStatePaymentSuccessful();
        claimIssuedDate = now();
        confirmationOfServiceDeadline = claimIssuedDate.plusMonths(4).atTime(23, 59, 59);
        ccdState = CREATED;
        return this;
    }

    public CaseDataBuilder atStateClaimStayed() {
        atStateClaimCreated();
        ccdState = STAYED;
        return this;
    }

    public CaseDataBuilder atStateServiceConfirmed() {
        atStateClaimCreated();
        deemedServiceDateToRespondentSolicitor1 = DEEMED_SERVICE_DATE;
        respondentSolicitor1ResponseDeadline = RESPONSE_DEADLINE;
        serviceMethodToRespondentSolicitor1 = ServiceMethodBuilder.builder().email().build();
        serviceLocationToRespondentSolicitor1 = ServiceLocation.builder().location(BUSINESS).build();
        serviceDateTimeToRespondentSolicitor1 = LocalDateTime.now();
        servedDocuments = List.of(CLAIM_FORM, PARTICULARS_OF_CLAIM, OTHER);
        servedDocumentsOther = "My other documents";
        applicant1ServiceStatementOfTruthToRespondentSolicitor1 = StatementOfTruthBuilder.builder().build();
        return this;
    }

    public CaseDataBuilder atStateRespondedToClaim() {
        atStateServiceConfirmed();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicantSolicitorResponseDeadlineToRespondentSolicitor1 = APPLICANT_RESPONSE_DEADLINE;
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        ccdState = AWAITING_CLAIMANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateProceedsOffline() {
        atStateRespondedToClaim();
        ccdState = PROCEEDS_WITH_OFFLINE_JOURNEY;
        return this;
    }

    public CaseDataBuilder atStateFullDefence() {
        atStateRespondedToClaim();
        applicant1ProceedWithClaim = YES;
        applicant1DefenceResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("claimant-response.pdf").build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateServiceAcknowledge() {
        atStateServiceConfirmed();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateExtensionRequested() {
        atStateServiceAcknowledge();
        respondentSolicitor1claimResponseExtensionProposedDeadline = now().plusDays(21);
        respondentSolicitor1claimResponseExtensionAlreadyAgreed = NO;
        respondentSolicitor1claimResponseExtensionReason = "Need little more time";
        return this;
    }

    public CaseDataBuilder atStateExtensionResponded() {
        atStateExtensionRequested();
        respondentSolicitor1claimResponseExtensionAccepted = NO;
        respondentSolicitor1claimResponseExtensionCounter = YES;
        respondentSolicitor1claimResponseExtensionCounterDate = now().plusDays(19);
        respondentSolicitor1claimResponseExtensionRejectionReason = "This seems reasonable";
        return this;
    }

    public CaseDataBuilder businessProcess(BusinessProcess businessProcess) {
        this.businessProcess = businessProcess;
        return this;
    }

    public static CaseDataBuilder builder() {
        return new CaseDataBuilder();
    }

    public CaseData build() {
        return CaseData.builder()
            // Create Claim
            .claimSubmittedDateTime(claimSubmittedDateTime)
            .claimIssuedDate(claimIssuedDate)
            .legacyCaseReference(legacyCaseReference)
            .confirmationOfServiceDeadline(confirmationOfServiceDeadline)
            .allocatedTrack(allocatedTrack)
            .solicitorReferences(solicitorReferences)
            .courtLocation(courtLocation)
            .claimValue(claimValue)
            .claimType(claimType)
            .claimTypeOther(claimTypeOther)
            .personalInjuryType(personalInjuryType)
            .personalInjuryTypeOther(personalInjuryTypeOther)
            .applicant1(applicant1)
            .respondent1(respondent1)
            .applicantSolicitor1ClaimStatementOfTruth(applicantSolicitor1ClaimStatementOfTruth)
            .paymentDetails(paymentDetails)
            // Confirm Service
            .deemedServiceDateToRespondentSolicitor1(deemedServiceDateToRespondentSolicitor1)
            .respondentSolicitor1ResponseDeadline(respondentSolicitor1ResponseDeadline)
            .serviceMethodToRespondentSolicitor1(serviceMethodToRespondentSolicitor1)
            .serviceLocationToRespondentSolicitor1(serviceLocationToRespondentSolicitor1)
            .servedDocuments(servedDocuments)
            .servedDocumentsOther(servedDocumentsOther)
            .servedDocumentFiles(servedDocumentFiles)
            .serviceDateToRespondentSolicitor1(serviceDateToRespondentSolicitor1)
            .serviceDateTimeToRespondentSolicitor1(serviceDateTimeToRespondentSolicitor1)
            .applicant1ServiceStatementOfTruthToRespondentSolicitor1(
                applicant1ServiceStatementOfTruthToRespondentSolicitor1
            )
            .serviceNamedPersonToRespondentSolicitor1(serviceNamedPersonToRespondentSolicitor1)
            // Acknowledge Service
            .respondent1ClaimResponseIntentionType(respondent1ClaimResponseIntentionType)
            // Request Extension
            .respondentSolicitor1claimResponseExtensionProposedDeadline(
                respondentSolicitor1claimResponseExtensionProposedDeadline
            )
            .respondentSolicitor1claimResponseExtensionAlreadyAgreed(
                respondentSolicitor1claimResponseExtensionAlreadyAgreed
            )
            .respondentSolicitor1claimResponseExtensionReason(respondentSolicitor1claimResponseExtensionReason)
            // Respond To Extension Request
            .respondentSolicitor1claimResponseExtensionAccepted(respondentSolicitor1claimResponseExtensionAccepted)
            .respondentSolicitor1claimResponseExtensionCounter(respondentSolicitor1claimResponseExtensionCounter)
            .respondentSolicitor1claimResponseExtensionRejectionReason(
                respondentSolicitor1claimResponseExtensionRejectionReason
            )
            .respondentSolicitor1claimResponseExtensionCounterDate(
                respondentSolicitor1claimResponseExtensionCounterDate
            )
            // Defendant Response
            .respondent1ClaimResponseType(respondent1ClaimResponseType)
            .respondent1ClaimResponseDocument(respondent1ClaimResponseDocument)
            .applicantSolicitorResponseDeadlineToRespondentSolicitor1(
                applicantSolicitorResponseDeadlineToRespondentSolicitor1
            )
            // Claimant Response
            .applicant1ProceedWithClaim(applicant1ProceedWithClaim)
            .applicant1DefenceResponseDocument(applicant1DefenceResponseDocument)

            .ccdState(ccdState)
            .businessProcess(businessProcess)
            .ccdCaseReference(ccdCaseReference)
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments)
            .withdrawClaim(withdrawClaim)
            .discontinueClaim(discontinueClaim)
            .respondent1DQ(respondent1DQ)
            .build();
    }
}
