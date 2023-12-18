package uk.gov.hmcts.reform.civil.service.docmosis.draft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.draft.ClaimForm;
import uk.gov.hmcts.reform.civil.model.docmosis.draft.ClaimFormMapper;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIMANT_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DRAFT_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.GENERATE_LIP_CLAIMANT_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.GENERATE_LIP_DEFENDANT_CLAIM_FORM;

@Service
@Getter
@RequiredArgsConstructor
public class ClaimFormGenerator implements TemplateDataGenerator<ClaimForm> {

    private final ClaimFormMapper claimFormMapper;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation, CaseEvent caseEvent) {
       DocmosisTemplates docmosisTemplates = getDocmosisTemplate(caseEvent);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            docmosisTemplates
        );

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(String.format(docmosisTemplates.getDocumentTitle(), caseData.getLegacyCaseReference()),
                    docmosisDocument.getBytes(),
                    getDocumentType(caseEvent)
            )
        );
    }
    private DocmosisTemplates getDocmosisTemplate(CaseEvent caseEvent) {
        return switch (caseEvent) {
            case GENERATE_DRAFT_FORM -> DRAFT_CLAIM_FORM;
            case GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC -> GENERATE_LIP_CLAIMANT_CLAIM_FORM;
            case GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC -> GENERATE_LIP_DEFENDANT_CLAIM_FORM;
            default -> throw new IllegalArgumentException(String.format("No Docmosis Template available for %s event", caseEvent));
        };
    }

    private DocumentType getDocumentType(CaseEvent caseEvent) {
        return switch (caseEvent) {
            case GENERATE_DRAFT_FORM -> DocumentType.DRAFT_CLAIM_FORM;
            case GENERATE_LIP_CLAIMANT_CLAIM_FORM_SPEC -> CLAIMANT_CLAIM_FORM;
            case GENERATE_LIP_DEFENDANT_CLAIM_FORM_SPEC -> SEALED_CLAIM;
            default -> throw new IllegalArgumentException(String.format("No DocumentType available for %s event", caseEvent));
        };
    }


    @Override
    public ClaimForm getTemplateData(CaseData caseData) {
        return claimFormMapper.toClaimForm(caseData);
    }
}
