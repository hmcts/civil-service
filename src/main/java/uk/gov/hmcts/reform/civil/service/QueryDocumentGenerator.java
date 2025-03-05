package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingForm;
import uk.gov.hmcts.reform.civil.model.docmosis.querymanagement.QueryMessageThread;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.QUERY_DOCUMENT;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.assignCategoryIdToQueryDocument;

@Service
@RequiredArgsConstructor
public class QueryDocumentGenerator implements TemplateDataGenerator<HearingForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final AssignCategoryId assignCategoryId;

    public List<CaseDocument> generate(List<Element<CaseMessage>> messageThread, String authorisation, List<String> roles) {

        List<CaseDocument> caseDocuments = new ArrayList<>();
        QueryMessageThread templateData = QueryMessageThread.builder().messages(messageThread).build();
        DocmosisTemplates template = QUERY_DOCUMENT;
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        CaseDocument caseDocument = documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                String.format(template.getDocumentTitle(), messageThread.get(0).getValue().getSubject()),
                document.getBytes(),
                DocumentType.HEARING_FORM
            )
        );
        assignCategoryIdToQueryDocument(caseDocument.getDocumentLink(), assignCategoryId, roles);
        caseDocument.setCreatedDatetime(messageThread.get(0).getValue().getCreatedOn());
        caseDocuments.add(caseDocument);
        return caseDocuments;
    }

    public List<CaseDocument> generate(List<Element<CaseMessage>> messageThread, String authorisation, String categoryId) {

        List<CaseDocument> caseDocuments = new ArrayList<>();
        QueryMessageThread templateData = QueryMessageThread.builder().messages(messageThread).build();
        DocmosisTemplates template = QUERY_DOCUMENT;
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        CaseDocument caseDocument = documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                String.format(template.getDocumentTitle(), messageThread.get(0).getValue().getSubject()),
                document.getBytes(),
                DocumentType.HEARING_FORM
            )
        );
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, categoryId);
        caseDocument.setCreatedDatetime(messageThread.get(0).getValue().getCreatedOn());
        caseDocuments.add(caseDocument);
        return caseDocuments;
    }
}

