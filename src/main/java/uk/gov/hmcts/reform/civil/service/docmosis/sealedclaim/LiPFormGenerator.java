package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.LiPForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.LITIGANT_IN_PERSON_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.LITIGANT_IN_PERSON_CLAIM_FORM;

@Service
@RequiredArgsConstructor
public class LiPFormGenerator implements TemplateDataGenerator<LiPForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {

        LiPForm formData = getTemplateData(caseData);
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(formData, LITIGANT_IN_PERSON_CLAIM_FORM);

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData),
                    docmosisDocument.getBytes(),
                    LITIGANT_IN_PERSON_FORM)
        );
    }

    private String getFileName(CaseData caseData) {
        return String.format(LITIGANT_IN_PERSON_CLAIM_FORM.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public LiPForm getTemplateData(CaseData caseData) {
        return LiPForm.builder()
            .ccdCaseReference(caseData.getCcdCaseReference().toString())
            .referenceNumber(caseData.getLegacyCaseReference())
            .build();
    }
}
