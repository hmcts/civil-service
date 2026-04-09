package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.ga.utils.DocUploadUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.RESPOND_FOR_INFORMATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondForInformationGenerator implements TemplateDataGenerator<JudgeDecisionPdfDocument> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private String role;

    public CaseDocument generate(GeneralApplicationCaseData caseData, String authorisation, String role) {
        this.role = role;
        JudgeDecisionPdfDocument templateData = getTemplateData(caseData, authorisation);

        DocmosisTemplates docmosisTemplate = getTemplate();

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
                templateData,
                docmosisTemplate
        );

        log.info("Generate respond for information for caseId: {}", caseData.getCcdCaseReference());
        return documentManagementService.uploadDocument(
                authorisation,
                new PDF(getFileName(docmosisTemplate), docmosisDocument.getBytes(),
                        DocumentType.REQUEST_FOR_INFORMATION)
        );
    }

    @Override
    public JudgeDecisionPdfDocument getTemplateData(GeneralApplicationCaseData caseData, String authorisation) {

        return new JudgeDecisionPdfDocument()
            .setClaimNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
            .setClaimant1Name(caseData.getClaimant1PartyName())
            .setDefendant1Name(caseData.getDefendant1PartyName())
            .setJudgeComments(caseData.getGeneralAppAddlnInfoText())
            .setJudgeNameTitle(getSubmittedBy(role, caseData))
            .setSubmittedOn(LocalDate.now());
    }

    private String getSubmittedBy(String role, GeneralApplicationCaseData caseData) {
        if (role.equals(DocUploadUtils.APPLICANT)) {
            return caseData.getApplicantPartyName();
        }
        return caseData.getClaimant1PartyName();
    }

    private String getFileName(DocmosisTemplates docmosisTemplate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(docmosisTemplate.getDocumentTitle(), LocalDateTime.now().format(formatter));
    }

    protected DocmosisTemplates getTemplate() {
        return RESPOND_FOR_INFORMATION;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
