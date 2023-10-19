package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;

@Service
@RequiredArgsConstructor
public class SendHearingBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final DocumentDownloadService documentDownloadService;
    private static final String HEARING_PACK_LETTER_TYPE = "hearing-document-pack";

    public void sendHearingToDefendantLIP(String authorisation, CaseData caseData) {
        if (caseData.getSystemGeneratedCaseDocuments() != null && !caseData.getSystemGeneratedCaseDocuments().isEmpty()) {
            Optional<Element<CaseDocument>> caseDocument = caseData.getHearingDocuments().stream().findFirst();

            if (caseDocument.isPresent()) {
                String documentUrl = caseDocument.get().getValue().getDocumentLink().getDocumentUrl();
                String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
                byte[] letterContent;
                try {
                    letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
                } catch (Exception e) {
                    log.error("Failed getting letter content for Hearing ");
                    throw new DocumentDownloadException(caseDocument.get().getValue().getDocumentName(), e);
                }
                List<String> recipients = List.of(caseData.getRespondent1().getPartyName());
                bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                             caseData.getLegacyCaseReference(), HEARING_PACK_LETTER_TYPE, recipients);
            }
        }
    }
}
