package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ORDER_NOTICE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendFinalOrderToLiPCallbackHandler.TASK_ID_DEFENDANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendFinalOrderBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final DocumentDownloadService documentDownloadService;
    private final FeatureToggleService featureToggleService;
    private static final String FINAL_ORDER_PACK_LETTER_TYPE = "final-order-document-pack";
    private static final String TRANSLATED_ORDER_PACK_LETTER_TYPE = "translated-order-document-pack";

    public void sendFinalOrderToLIP(String authorisation, CaseData caseData, String task) {
        if (checkFinalOrderDocumentAvailable(caseData)) {
            Document document = caseData.getFinalOrderDocumentCollection().get(0).getValue().getDocumentLink();
            sendBulkPrint(authorisation, caseData, task, document, FINAL_ORDER_PACK_LETTER_TYPE);
        }
    }

    public void sendTranslatedFinalOrderToLIP(String authorisation, CaseData caseData, String task) {
        if (checkTranslatedFinalOrderDocumentAvailable(caseData, task)) {
            Document document = caseData.getSystemGeneratedCaseDocuments()
                .get(caseData.getSystemGeneratedCaseDocuments().size() - 1).getValue().getDocumentLink();
            sendBulkPrint(authorisation, caseData, task, document, TRANSLATED_ORDER_PACK_LETTER_TYPE);
        }
    }

    private void sendBulkPrint(String authorisation, CaseData caseData, String task, Document document,
                               String letterType) {
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
                                     caseData.getLegacyCaseReference(), letterType, recipients);
    }

    private boolean checkFinalOrderDocumentAvailable(CaseData caseData) {
        return nonNull(caseData.getSystemGeneratedCaseDocuments())
            && !caseData.getSystemGeneratedCaseDocuments().isEmpty()
            && nonNull(caseData.getFinalOrderDocumentCollection())
            && !caseData.getFinalOrderDocumentCollection().isEmpty();
    }

    private boolean checkTranslatedFinalOrderDocumentAvailable(CaseData caseData, String task) {
        if (featureToggleService.isCaseProgressionEnabled()) {
            List<Element<CaseDocument>> systemGeneratedDocuments = caseData.getSystemGeneratedCaseDocuments();
            return (!caseData.getSystemGeneratedCaseDocuments().isEmpty())
                && isEligibleToGetTranslatedOrder(caseData, task)
                && systemGeneratedDocuments
                .get(systemGeneratedDocuments.size() - 1).getValue().getDocumentType().equals(ORDER_NOTICE_TRANSLATED_DOCUMENT);
        }
        return false;
    }

    private boolean isDefendantPrint(String task) {
        return task.equals(TASK_ID_DEFENDANT);
    }

    private List<String> getRecipientsList(CaseData caseData, String task) {
        return isDefendantPrint(task) ? List.of(caseData.getRespondent1().getPartyName())
            : List.of(caseData.getApplicant1().getPartyName());
    }

    private boolean isEligibleToGetTranslatedOrder(CaseData caseData, String task) {
        if (isDefendantPrint(task)) {
            return caseData.isRespondent1NotRepresented() && caseData.isRespondentResponseBilingual();
        } else {
            return caseData.isApplicant1NotRepresented() && caseData.isClaimantBilingual();
        }
    }
}
