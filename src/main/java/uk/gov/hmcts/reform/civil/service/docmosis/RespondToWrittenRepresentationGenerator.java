package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.utils.DocUploadUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.RESPOND_FOR_WRITTEN_REPRESENTATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondToWrittenRepresentationGenerator implements TemplateDataGenerator<JudgeDecisionPdfDocument>  {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private String role;

    public CaseDocument generate(CaseData caseData, String authorisation, String role) {
        this.role = role;
        JudgeDecisionPdfDocument templateData = getTemplateData(caseData, authorisation);

        DocmosisTemplates docmosisTemplate = getDocmosisTemplate();

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            docmosisTemplate
        );
        log.info("Generate respond to written representation document for caseId: {}", caseData.getCcdCaseReference());

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(docmosisTemplate), docmosisDocument.getBytes(),
                    DocumentType.WRITTEN_REPRESENTATION_SEQUENTIAL)
        );
    }

    @Override
    public JudgeDecisionPdfDocument getTemplateData(CaseData caseData, String authorisation) {

        JudgeDecisionPdfDocument.JudgeDecisionPdfDocumentBuilder respondToWrittenRepDocumentBuilder =
            JudgeDecisionPdfDocument.builder()
                .claimNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
                .claimant1Name(caseData.getClaimant1PartyName())
                .defendant1Name(caseData.getDefendant1PartyName())
                .judgeComments(caseData.getGeneralAppWrittenRepText())
                .judgeNameTitle(getSubmittedBy(role, caseData))
                .submittedOn(LocalDate.now());

        return respondToWrittenRepDocumentBuilder.build();
    }

    private String getSubmittedBy(String role, CaseData caseData) {
        if (role.equals(DocUploadUtils.APPLICANT)) {
            return caseData.getApplicantPartyName();
        }
        return YES.equals(caseData.getParentClaimantIsApplicant()) ? caseData.getDefendant1PartyName() :
                caseData.getClaimant1PartyName();
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    protected DocmosisTemplates getDocmosisTemplate() {
        return RESPOND_FOR_WRITTEN_REPRESENTATION;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
