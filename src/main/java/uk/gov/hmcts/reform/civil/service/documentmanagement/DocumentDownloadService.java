package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentDownloadService {

    private final DocumentManagementService documentManagementService;

    public DownloadedDocumentResponse downloadDocument(String authorisation, String documentId) {
        String documentPath = String.format("documents/%s", documentId);
        log.info("Logging DownloadedDocumentResponse userTokem: {} ", authorisation);
        return documentManagementService.downloadDocumentWithMetaData(authorisation, documentPath);
    }

    public void validateEncryptionOnUploadedDocument(Document document,
                                                     String authorisation,
                                                     Long caseId,
                                                     List<String> errors) {
        log.info("CaseId: {} ", caseId);
        log.info("Logging userTokem: {} ", authorisation);
        if (document != null) {
            String documentFilename = document.getDocumentFileName();
            log.info("checking password protection for file {} for Case ID: {}", documentFilename, caseId);

            byte[] letterContent;
            String documentId = document.getDocumentUrl()
                .substring(document.getDocumentUrl().lastIndexOf("/") + 1);
            try {
                letterContent = downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
            } catch (IOException | DocumentDownloadException e) {
                String formatError = String.format("Failed getting letter content for filename %s caseId %s ",
                                                   document.getDocumentFileName(), caseId);
                log.error(formatError);
                throw new DocumentDownloadException(document.getDocumentFileName(), e);
            }

            try (PDDocument doc = Loader.loadPDF(letterContent)) {
                if (doc.isEncrypted()) {
                    log.info("Uploaded document {} contains some kind of encryption.", documentFilename);
                }
            } catch (InvalidPasswordException ipe) {
                String errorMessage = "Uploaded document '" + documentFilename + "' is password protected."
                    + " Please remove password and try uploading again.";
                errors.add(errorMessage);
                log.error(ipe.getMessage());
            } catch (IOException exc) {
                String errorMessage = "Failed to parse the documents " + documentFilename + " for caseId" + caseId;
                errors.add(errorMessage + "; " + exc.getMessage());
                log.error(exc.getMessage());
            }
        }
    }
}
