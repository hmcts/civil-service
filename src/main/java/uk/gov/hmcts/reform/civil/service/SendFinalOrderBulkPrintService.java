package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendFinalOrderToLiPCallbackHandler.TASK_ID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;

@Service
@RequiredArgsConstructor
public class SendFinalOrderBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final DocumentDownloadService documentDownloadService;
    private static final String FINAL_ORDER_PACK_LETTER_TYPE = "final-order-document-pack";

    public void sendFinalOrderToLIP(String authorisation, CaseData caseData, String task) {
        if (checkFinalOrderDocumentAvailable(caseData)) {
            Document document = caseData.getFinalOrderDocument();
            String documentUrl = document.getDocumentUrl();
            String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
            byte[] letterContent;
            try {
                letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
            } catch (Exception e) {
                log.error("Failed getting letter content for Final Order ");
                throw new DocumentDownloadException(document.getDocumentFileName(), e);
            }
            List<String> recipients = getRecipientsList(caseData, task);
            bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                         caseData.getLegacyCaseReference(), FINAL_ORDER_PACK_LETTER_TYPE, recipients);
        }
    }

    private boolean checkFinalOrderDocumentAvailable(CaseData caseData) {
        return nonNull(caseData.getSystemGeneratedCaseDocuments())
            && !caseData.getSystemGeneratedCaseDocuments().isEmpty()
            && nonNull(caseData.getFinalOrderDocument());
    }

    private boolean isDefendantPrint(String task) {
        return task.equals(TASK_ID_DEFENDANT);
    }

    private List<String> getRecipientsList(CaseData caseData, String task) {
        return isDefendantPrint(task) ? List.of(caseData.getRespondent1().getPartyName())
            : List.of(caseData.getApplicant1().getPartyName());
    }
}
