package uk.gov.hmcts.reform.civil.service.docmosis.judgment;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_DETERMINATION;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgment.JudgmentByDeterminationOrAdmission;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

@Service
@RequiredArgsConstructor
public class JudgmentGenerator implements TemplateDataGenerator<JudgmentByDeterminationOrAdmission> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    @Override
    public JudgmentByDeterminationOrAdmission getTemplateData(CaseData caseData) {
        return JudgmentByDeterminationOrAdmission.builder()
            .caseNumber("1234").build();
    }

    public CaseDocument generate(final CaseData caseData, final String authorization) {
        JudgmentByDeterminationOrAdmission templateData = getTemplateData(caseData);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            JUDGMENT_BY_DETERMINATION
        );
        String fileName = String.format(
            JUDGMENT_BY_DETERMINATION.getDocumentTitle(),
            caseData.getLegacyCaseReference()
        );

        return documentManagementService.uploadDocument(
            authorization,
            new PDF(fileName, docmosisDocument.getBytes(), DocumentType.DEFAULT_JUDGMENT)
        );
    }

}
