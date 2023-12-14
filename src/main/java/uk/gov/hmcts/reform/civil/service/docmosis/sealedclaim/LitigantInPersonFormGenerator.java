package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.LitigantInPersonForm;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.LIP_CLAIM_FORM;

@Service
@RequiredArgsConstructor
public class LitigantInPersonFormGenerator implements TemplateDataGenerator<LitigantInPersonForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {

        LitigantInPersonForm formData = getTemplateData(caseData);
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(formData, LIP_CLAIM_FORM);

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(String.format(LIP_CLAIM_FORM.getDocumentTitle(), caseData.getLegacyCaseReference()),
                    docmosisDocument.getBytes(),
                    DocumentType.LITIGANT_IN_PERSON_CLAIM_FORM
            )
        );
    }

    @Override
    public LitigantInPersonForm getTemplateData(CaseData caseData) {
        return LitigantInPersonForm.builder()
            .ccdCaseReference(caseData.getCcdCaseReference().toString())
            .referenceNumber(caseData.getLegacyCaseReference())
            .build();
    }
}
