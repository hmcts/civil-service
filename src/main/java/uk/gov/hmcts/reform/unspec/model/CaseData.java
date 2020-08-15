package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.ClaimType;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim.SealedClaimFormGenerator.REFERENCE_NUMBER;

@Data
@Builder(toBuilder = true)
public class CaseData {

    private final Long ccdCaseReference;
    private final SolicitorReferences solicitorReferences;
    private final CourtLocation courtLocation;
    private final Party claimant;
    private final Party claimant2;
    private final Party respondent;
    private final Party respondent2;
    private final ClaimValue claimValue;
    private final ClaimType claimType;
    private final StatementOfTruth claimStatementOfTruth;
    private final StatementOfTruth serviceStatementOfTruth;
    private final List<Element<CaseDocument>> systemGeneratedCaseDocuments;
    private final ServiceMethod serviceMethod;
    private final LocalDate serviceDate;
    private final LocalDateTime serviceDateAndTime;
    private final LocalDateTime claimSubmittedDateTime;
    private final LocalDate claimIssuedDate;
    private final LocalDate deemedDateOfService;
    private final LocalDateTime responseDeadline;
    private final List<ServedDocuments> servedDocuments;
    //TODO this will be stored in database while reading sequence number of OCMC for Case man reference number
    private final String legacyCaseReference = REFERENCE_NUMBER;
    private final ServiceLocation serviceLocation;
    private final ServedDocumentFiles servedDocumentFiles;
    private final String servedDocumentsOther;
}
