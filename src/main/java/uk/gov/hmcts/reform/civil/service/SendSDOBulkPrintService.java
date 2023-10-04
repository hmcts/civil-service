package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;

@Service
@RequiredArgsConstructor
public class SendSDOBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final DocumentDownloadService documentDownloadService;
    private static final String SDO_ORDER_PACK_LETTER_TYPE = "sdo-order-pack";

    public void sendSDOToDefendantLIP(String authorisation, CaseData caseData) {
        if (caseData.getSystemGeneratedCaseDocuments() != null && !caseData.getSystemGeneratedCaseDocuments().isEmpty()) {
            Optional<Element<CaseDocument>> caseDocument = caseData.getSDODocument();

            if (caseDocument.isPresent()) {
                String documentUrl = caseDocument.get().getValue().getDocumentLink().getDocumentUrl();
                String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
                byte[] letterContent;
                try {
                    letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
                } catch (IOException e) {
                    log.error("Failed getting letter content for SDO ");
                    throw new DocumentDownloadException(caseDocument.get().getValue().getDocumentName(), e);
                }
                List<String> recipients = Arrays.asList(caseData.getRespondent1().getPartyName());
                bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                             caseData.getLegacyCaseReference(), SDO_ORDER_PACK_LETTER_TYPE, recipients);
            }
        }
    }
}
