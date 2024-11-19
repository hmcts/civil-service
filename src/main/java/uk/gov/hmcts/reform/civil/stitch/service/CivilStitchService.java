package uk.gov.hmcts.reform.civil.stitch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.stitch.PdfMerger;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CivilStitchService {

    private final DocumentManagementService managementService;

    public CaseDocument generateStitchedCaseDocument(List<DocumentMetaData> documents,
                                                     String documentName,
                                                     Long caseId,
                                                     String authorisation) {

        log.info("Generating stitched case document for caseId {} with filename {}", caseId, documentName);
        List<byte[]> docs = documents.stream()
            .map(doc -> managementService.downloadDocument(authorisation, doc.getDocument().getDocumentUrl())).toList();

        byte[] bytes = PdfMerger.mergeDocuments(docs, String.valueOf(caseId));

        return managementService.uploadDocument(authorisation, getPdf(bytes, documentName));
    }

    private PDF getPdf(byte[] bytes, String documentName) {
        return new PDF(documentName, bytes, DocumentType.SEALED_CLAIM);
    }
}