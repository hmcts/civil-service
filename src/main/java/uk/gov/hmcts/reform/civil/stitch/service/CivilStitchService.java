package uk.gov.hmcts.reform.civil.stitch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.DocumentConversionService;
import uk.gov.hmcts.reform.civil.stitch.PdfMerger;
import uk.gov.hmcts.reform.civil.stitch.PdfMergeException;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CivilStitchService {

    private final DocumentManagementService managementService;
    private final DocumentConversionService conversionService;

    public CaseDocument generateStitchedCaseDocument(List<DocumentMetaData> documents,
                                                     String documentName,
                                                     Long caseId,
                                                     DocumentType documentType,
                                                     String authorisation) {

        log.info("Generating stitched case document for caseId {} with filename {}", caseId, documentName);
        List<byte[]> docs = documents.stream()
            .map(doc -> convertAndValidate(doc.getDocument(), caseId, authorisation)).toList();

        byte[] bytes = PdfMerger.mergeDocuments(docs, String.valueOf(caseId));

        return managementService.uploadDocument(authorisation, getPdf(bytes, documentName, documentType));
    }

    private byte[] convertAndValidate(Document document, Long caseId, String authorisation) {
        byte[] bytes = conversionService.convertDocumentToPdf(document, caseId, authorisation);
        try (PDDocument pdf = Loader.loadPDF(bytes == null ? new byte[0] : bytes)) {
            return bytes;
        } catch (IOException exception) {
            String url = document.getDocumentUrl();
            String documentId = url == null ? "unknown" : url.substring(url.lastIndexOf('/') + 1);
            throw new PdfMergeException(
                "Unable to parse PDF document '" + document.getDocumentFileName()
                    + "' (CDAM id: " + documentId + ") for caseId " + caseId,
                exception
            );
        }
    }

    private PDF getPdf(byte[] bytes, String documentName, DocumentType documentType) {
        return new PDF(documentName, bytes, documentType);
    }
}
