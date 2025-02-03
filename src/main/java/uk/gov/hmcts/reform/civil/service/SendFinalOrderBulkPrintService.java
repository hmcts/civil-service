package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.CoverLetterAppendService;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.ORDER_NOTICE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendFinalOrderToLiPCallbackHandler.TASK_ID_DEFENDANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendFinalOrderBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final FeatureToggleService featureToggleService;
    private final CoverLetterAppendService coverLetterAppendService;
    private static final String FINAL_ORDER_PACK_LETTER_TYPE = "final-order-document-pack";
    private static final String TRANSLATED_ORDER_PACK_LETTER_TYPE = "translated-order-document-pack";

    public void sendFinalOrderToLIP(String authorisation, CaseData caseData, String task) {
        if (checkFinalOrderDocumentAvailable(caseData)) {
            CaseDocument document = caseData.getFinalOrderDocumentCollection().get(0).getValue();
            sendBulkPrint(authorisation, caseData, task, document, FINAL_ORDER_PACK_LETTER_TYPE);
        }
    }

    public void sendTranslatedFinalOrderToLIP(String authorisation, CaseData caseData, String task) {
        if (checkTranslatedFinalOrderDocumentAvailable(caseData, task)) {
            CaseDocument document = caseData.getSystemGeneratedCaseDocuments()
                .get(caseData.getSystemGeneratedCaseDocuments().size() - 1).getValue();
            sendBulkPrint(authorisation, caseData, task, document, TRANSLATED_ORDER_PACK_LETTER_TYPE);
        }
    }

    private void sendBulkPrint(String authorisation, CaseData caseData, String task, CaseDocument caseDocument,
                               String letterType) {
        byte[] letterContent;
        Party recipient = isDefendantPrint(task) ? caseData.getRespondent1() : caseData.getApplicant1();

        letterContent = coverLetterAppendService.makeDocumentMailable(caseData, authorisation, recipient, JUDGE_FINAL_ORDER, caseDocument);

        List<String> recipients = List.of(recipient.getPartyName());
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

    private boolean isEligibleToGetTranslatedOrder(CaseData caseData, String task) {
        if (isDefendantPrint(task)) {
            return caseData.isRespondent1NotRepresented() && caseData.isRespondentResponseBilingual();
        } else {
            return caseData.isApplicant1NotRepresented() && caseData.isClaimantBilingual();
        }
    }
}
