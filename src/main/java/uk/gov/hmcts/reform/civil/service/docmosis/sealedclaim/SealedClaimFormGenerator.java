package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;

@Service
@RequiredArgsConstructor
public class SealedClaimFormGenerator implements TemplateDataGenerator<SealedClaimForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final RepresentativeService representativeService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        SealedClaimForm templateData = getTemplateData(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N1);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }

    private String getFileName(CaseData caseData) {
        return String.format(N1.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public SealedClaimForm getTemplateData(CaseData caseData) {
        Optional<SolicitorReferences> solicitorReferences = ofNullable(caseData.getSolicitorReferences());
        return SealedClaimForm.builder()
            .applicants(getApplicants(caseData))
            .respondents(getRespondents(caseData))
            .claimValue(caseData.getClaimValue().formData())
            .statementOfTruth(caseData.getApplicantSolicitor1ClaimStatementOfTruth())
            .claimDetails(caseData.getDetailsOfClaim())
            .hearingCourtLocation(caseData.getCourtLocation().getApplicantPreferredCourt())
            .applicantRepresentative(representativeService.getApplicantRepresentative(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .issueDate(caseData.getIssueDate())
            .submittedOn(caseData.getSubmittedDate().toLocalDate())
            .applicantExternalReference(solicitorReferences
                                            .map(SolicitorReferences::getApplicantSolicitor1Reference)
                                            .orElse(""))
            .respondentExternalReference(solicitorReferences
                                             .map(SolicitorReferences::getRespondentSolicitor1Reference)
                                             .orElse(""))
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .build();
    }

    private List<Party> getRespondents(CaseData caseData) {
        uk.gov.hmcts.reform.civil.model.Party respondent = caseData.getRespondent1();
        return List.of(Party.builder()
                           .name(respondent.getPartyName())
                           .primaryAddress(respondent.getPrimaryAddress())
                           .representative(representativeService.getRespondentRepresentative(caseData))
                           .build());
    }

    private List<Party> getApplicants(CaseData caseData) {
        uk.gov.hmcts.reform.civil.model.Party applicant = caseData.getApplicant1();
        return List.of(Party.builder()
                           .name(applicant.getPartyName())
                           .primaryAddress(applicant.getPrimaryAddress())
                           .litigationFriendName(
                               ofNullable(caseData.getApplicant1LitigationFriend())
                                   .map(LitigationFriend::getFullName)
                                   .orElse(""))
                           .representative(representativeService.getApplicantRepresentative(caseData))
                           .build());
    }
}
