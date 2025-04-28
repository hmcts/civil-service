package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.querymanagement.QueryDocument;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.QUERY_DOCUMENT;

@Service
@RequiredArgsConstructor
public class QueryDocumentGenerator {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final AssignCategoryId assignCategoryId;

    public CaseDocument generate(Long caseId, List<Element<CaseMessage>> messageThread, String authorisation, DocCategory documentCategory) {

        QueryDocument templateData = QueryDocument.from(caseId.toString(), messageThread);
        DocmosisTemplates template = QUERY_DOCUMENT;
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        CaseDocument caseDocument = documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                String.format(template.getDocumentTitle(), messageThread.get(0).getValue().getSubject()),
                document.getBytes(),
                DocumentType.QUERY_DOCUMENT
            )
        );
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, documentCategory.getValue());
        caseDocument.setCreatedDatetime(messageThread.get(0).getValue().getCreatedOn());
        return caseDocument;
    }
}

