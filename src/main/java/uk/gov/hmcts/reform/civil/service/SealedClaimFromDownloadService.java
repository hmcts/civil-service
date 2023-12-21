package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;

@Service
@RequiredArgsConstructor
public class SealedClaimFromDownloadService {
    private final DocumentDownloadService documentDownloadService;

    public byte[] downloadDocument(String authorisation, CaseData caseData) {
        if (caseData.getSystemGeneratedCaseDocuments() != null && !caseData.getSystemGeneratedCaseDocuments().isEmpty()) {
            Optional<Element<CaseDocument>> caseDocument = caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.SEALED_CLAIM)).findAny();
            log.debug("----------- SealedClaimFromDownloadService - document exist -----------");
            if (caseDocument.isPresent()) {
                log.debug("----------- SealedClaimFromDownloadService - document isPresent -----------");
                String documentUrl = caseDocument.get().getValue().getDocumentLink().getDocumentUrl();
                String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
                try {
                    return documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
                } catch (IOException e) {
                    log.error("Failed getting sealed claim form ");
                    throw new DocumentDownloadException(caseDocument.get().getValue().getDocumentName(), e);
                }
            }
        }
        return new byte[0];
    }
}
