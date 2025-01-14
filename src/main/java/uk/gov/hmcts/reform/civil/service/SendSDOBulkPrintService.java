package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoCoverLetterAppendService;
import uk.gov.hmcts.reform.civil.model.Party;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendSDOToLiPDefendantCallbackHandler.TASK_ID_DEFENDANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendSDOBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final SdoCoverLetterAppendService sdoCoverLetterAppendService;
    private static final String SDO_ORDER_PACK_LETTER_TYPE = "sdo-order-pack";

    public void sendSDOOrderToLIP(String authorisation, CaseData caseData, String taskId) {
        if (caseData.getSystemGeneratedCaseDocuments() != null && !caseData.getSystemGeneratedCaseDocuments().isEmpty()) {
            Optional<Element<CaseDocument>> caseDocument = caseData.getSDODocument();

            if (caseDocument.isPresent()) {
                byte[] letterContent;
                Party recipient = getPartyDetails(taskId, caseData);

                letterContent = sdoCoverLetterAppendService.makeSdoDocumentMailable(caseData, authorisation, recipient, SDO_ORDER, caseDocument.get().getValue());

                List<String> recipients = getRecipientsList(caseData, taskId);
                bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                             caseData.getLegacyCaseReference(), SDO_ORDER_PACK_LETTER_TYPE, recipients);
            }
        }
    }

    private List<String> getRecipientsList(CaseData caseData, String taskId) {
        return TASK_ID_DEFENDANT.equals(taskId) ? List.of(caseData.getRespondent1().getPartyName())
            : List.of(caseData.getApplicant1().getPartyName());
    }

    private Party getPartyDetails(String taskId, CaseData caseData) {
        return TASK_ID_DEFENDANT.equals(taskId) ? caseData.getRespondent1() : caseData.getApplicant1();
    }
}
