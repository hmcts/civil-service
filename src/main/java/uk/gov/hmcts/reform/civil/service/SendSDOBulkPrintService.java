package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;

import java.io.IOException;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;

@Service
@RequiredArgsConstructor
public class SendSDOBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;
    private static final String SDO_ORDER_PACK_LETTER_TYPE = "sdo-order-pack";

    public void sendSDOToDefendantLIP(CaseData caseData) {
        if (caseData.getSystemGeneratedCaseDocuments() != null && !caseData.getSystemGeneratedCaseDocuments().isEmpty()) {
            Optional<Element<CaseDocument>> caseDocument = caseData.getSDODocument();

            if (caseDocument.isPresent()) {
                String documentUrl = caseDocument.get().getValue().getDocumentLink().getDocumentUrl();
                String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
                byte[] letterContent;
                try {
                    letterContent = sealedClaimFormGeneratorForSpec.downloadDocument(documentId).file().getInputStream().readAllBytes();
                } catch (IOException e) {
                    log.error("Failed getting letter content for SDO ");
                    throw new DocumentDownloadException(caseDocument.get().getValue().getDocumentName(), e);
                }
                bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(), caseData.getLegacyCaseReference(), SDO_ORDER_PACK_LETTER_TYPE);
            }
        }
    }
}
