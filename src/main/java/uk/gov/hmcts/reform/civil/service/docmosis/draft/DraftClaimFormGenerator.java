package uk.gov.hmcts.reform.civil.service.docmosis.draft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.draft.DraftClaimForm;
import uk.gov.hmcts.reform.civil.model.docmosis.draft.DraftClaimFormMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DRAFT_CLAIM_FORM;

@Service
@Getter
@RequiredArgsConstructor
public class DraftClaimFormGenerator implements TemplateDataGenerator<DraftClaimForm> {

    private final DraftClaimFormMapper draftClaimFormMapper;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            DRAFT_CLAIM_FORM
        );
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(String.format(DRAFT_CLAIM_FORM.getDocumentTitle(), caseData.getLegacyCaseReference()),
                    docmosisDocument.getBytes(),
                    DocumentType.DRAFT_CLAIM_FORM
            )
        );
    }

    @Override
    public DraftClaimForm getTemplateData(CaseData caseData) {
        return draftClaimFormMapper.toDraftClaimForm(caseData);
    }
}
