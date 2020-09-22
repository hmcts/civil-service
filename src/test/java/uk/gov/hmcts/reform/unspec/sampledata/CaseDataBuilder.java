package uk.gov.hmcts.reform.unspec.sampledata;

import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;
import uk.gov.hmcts.reform.unspec.enums.CaseState;
import uk.gov.hmcts.reform.unspec.enums.ClaimType;
import uk.gov.hmcts.reform.unspec.enums.DefendantResponseType;
import uk.gov.hmcts.reform.unspec.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.unspec.enums.ResponseIntention;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;
import uk.gov.hmcts.reform.unspec.model.CourtLocation;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.ResponseDocument;
import uk.gov.hmcts.reform.unspec.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.unspec.model.ServiceLocation;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.AWAITING_CLAIMANT_INTENTION;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.CREATED;
import static uk.gov.hmcts.reform.unspec.enums.CaseState.STAYED;
import static uk.gov.hmcts.reform.unspec.enums.PersonalInjuryType.ROAD_ACCIDENT;
import static uk.gov.hmcts.reform.unspec.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.CLAIM_FORM;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.OTHER;
import static uk.gov.hmcts.reform.unspec.enums.ServedDocuments.PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.ServiceLocationType.BUSINESS;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;

public class CaseDataBuilder {

    // Create Claim
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
    private DefendantResponseType respondent1ClaimResponseType;
    private ResponseDocument respondent1ClaimResponseDocument;
    private LocalDateTime applicantSolicitorResponseDeadlineToRespondentSolicitor1;
    // Claimant Response
    private YesOrNo applicant1ProceedWithClaim;
    private ResponseDocument applicant1DefenceResponseDocument;
    private String applicant1NotProceedingReason;

    public CaseDataBuilder atStateClaimDraft() {
        solicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference("12345")
            .respondentSolicitor1Reference("6789")
            .build();
        courtLocation = CourtLocation.builder()
            .applicantPreferredCourt("The court location")
            .build();
        claimValue = ClaimValue.builder()
            .lowerValue(BigDecimal.valueOf(10000))
            .higherValue(BigDecimal.valueOf(100000))
            .build();
        claimType = ClaimType.PERSONAL_INJURY;
        personalInjuryType = ROAD_ACCIDENT;
        applicant1 = PartyBuilder.builder().individual().build();
        respondent1 = PartyBuilder.builder().soleTrader().build();
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.builder().build();

        return this;
    }

    public CaseDataBuilder atStateClaimCreated() {
        atStateClaimDraft();
        claimSubmittedDateTime = LocalDateTime.now();
        claimIssuedDate = LocalDate.now();
        confirmationOfServiceDeadline = claimIssuedDate.plusMonths(4).atTime(23, 59, 59);
        legacyCaseReference = "000LR001";
        allocatedTrack = FAST_CLAIM;
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
        deemedServiceDateToRespondentSolicitor1 = LocalDate.now();
        respondentSolicitor1ResponseDeadline = LocalDate.now().plusDays(14).atTime(23, 59, 59);
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
        respondent1ClaimResponseType = DefendantResponseType.FULL_DEFENCE;
        applicantSolicitorResponseDeadlineToRespondentSolicitor1 = LocalDateTime.now().plusDays(120);
        respondent1ClaimResponseDocument = ResponseDocument.builder()
            .file(DocumentBuilder.builder().documentName("defendant-response.pdf").build())
            .build();
        ccdState = AWAITING_CLAIMANT_INTENTION;
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
        respondentSolicitor1claimResponseExtensionProposedDeadline = LocalDate.now().plusDays(21);
        respondentSolicitor1claimResponseExtensionAlreadyAgreed = NO;
        respondentSolicitor1claimResponseExtensionReason = "Need little more time";
        return this;
    }

    public CaseDataBuilder atStateExtensionResponded() {
        atStateExtensionRequested();
        respondentSolicitor1claimResponseExtensionAccepted = NO;
        respondentSolicitor1claimResponseExtensionCounter = YES;
        respondentSolicitor1claimResponseExtensionCounterDate = LocalDate.now().plusDays(19);
        respondentSolicitor1claimResponseExtensionRejectionReason = "This seems reasonable";
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
            // Defendant Response
            .respondent1ClaimResponseType(respondent1ClaimResponseType)
            .respondent1ClaimResponseDocument(respondent1ClaimResponseDocument)
            .applicantSolicitorResponseDeadlineToRespondentSolicitor1(
                applicantSolicitorResponseDeadlineToRespondentSolicitor1
            )
            // Claimant Response
            .applicant1ProceedWithClaim(applicant1ProceedWithClaim)
            .applicant1DefenceResponseDocument(applicant1DefenceResponseDocument)
            .applicant1NotProceedingReason(applicant1NotProceedingReason)

            .ccdState(ccdState)
            .build();
    }
}
