package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimForm;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimFormForSpec;
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
import static uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions.BREAK_DOWN_INTEREST;
import static uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions.SAME_RATE_INTEREST;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;

@Service
@RequiredArgsConstructor
public class SealedClaimFormGeneratorForSpec implements TemplateDataGenerator<SealedClaimFormForSpec> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final RepresentativeService representativeService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        SealedClaimFormForSpec templateData = getTemplateData(caseData);

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
    public SealedClaimFormForSpec getTemplateData(CaseData caseData) {
        Optional<SolicitorReferences> solicitorReferences = ofNullable(caseData.getSolicitorReferences());
        return SealedClaimFormForSpec.builder()
            .referenceNumber(caseData.getLegacyCaseReference())
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .applicantExternalReference(solicitorReferences
                                            .map(SolicitorReferences::getApplicantSolicitor1Reference)
                                            .orElse(""))
            .respondentExternalReference(solicitorReferences
                                             .map(SolicitorReferences::getRespondentSolicitor1Reference)
                                             .orElse(""))
            .issueDate(caseData.getIssueDate())
            .submittedOn(caseData.getSubmittedDate().toLocalDate())
            .applicants(getApplicants(caseData))
            .respondents(getRespondents(caseData))
            .timeline(null) //caseData.getTimelineOfEvents().getValue())
            .sameInterestRate(caseData.getInterestClaimOptions().equals(SAME_RATE_INTEREST) + "")
            .breakdownInterestRate(caseData.getInterestClaimOptions().equals(BREAK_DOWN_INTEREST) + "")
            .totalInterestAmount(caseData.getTotalInterest() + "")
            .howTheInterestWasCalculated(caseData.getInterestClaimOptions().getDescription())
            .interestRate(caseData.getSameRateInterestSelection().getDifferentRate() != null ?
                              caseData.getSameRateInterestSelection().getDifferentRate()+"" :
                              "8%")
            .interestExplanationText(caseData.getSameRateInterestSelection()
                                         .getSameRateInterestType().getDescription())
            .interestFromDate(caseData.getInterestFromSpecificDate())
            .whenAreYouClaimingInterestFrom(caseData.getInterestClaimFrom() +"")
            .interestEndDateDescription(caseData.getInterestClaimUntil() +"")
            .totalClaimAmount(caseData.getTotalClaimAmount() +"")
            .interestAmount(caseData.getTotalInterest() +"")
            .claimFee(caseData.getClaimFee().getCalculatedAmountInPence() +"")
            .totalAmountOfClaim(caseData.getTotalClaimAmount() +"")
            .statementOfTruth(caseData.getUiStatementOfTruth())
//            .defendantResponseDeadlineDate(caseData.getRespondent1ResponseDeadline())

            .descriptionOfClaim("test")
       //     .claimAmount("testss")
          //  .interestEndDate("test")
            //.applicantRepresentativeOrganisationName("test")


            .build();
    }

    private List<Party> getRespondents(CaseData caseData) {
        var respondent = caseData.getRespondent1();
        return List.of(Party.builder()
                           .name(respondent.getPartyName())
                           .primaryAddress(respondent.getPrimaryAddress())
                           .representative(representativeService.getRespondentRepresentative(caseData))
                           .build());
    }

    private List<Party> getApplicants(CaseData caseData) {
        var applicant = caseData.getApplicant1();
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
