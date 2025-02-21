package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.docmosis.CoverLetterAppendService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendHearingToLiPCallbackHandler.TASK_ID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendHearingToLiPCallbackHandler.TASK_ID_DEFENDANT_HMC;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendHearingBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final DocumentDownloadService documentDownloadService;
    private final CoverLetterAppendService coverLetterAppendService;
    private static final String HEARING_PACK_LETTER_TYPE = "hearing-document-pack";

    public void sendHearingToLIP(String authorisation, CaseData caseData, String task, boolean welshDocument) {
        CaseDocument caseDocument;
        byte[] letterContent;
        if (!welshDocument && checkHearingDocumentAvailable(caseData)) {
            caseDocument = caseData.getHearingDocuments().get(0).getValue();
            letterContent = generateLetterContent(authorisation, caseData, task, caseDocument);
        } else if (welshDocument && checkWelshHearingDocumentAvailable(caseData)) {
            CaseDocument[] caseDocuments = {caseData.getHearingDocuments().get(0).getValue(),
                caseData.getHearingDocumentsWelsh().get(0).getValue()};
            letterContent = generateLetterContent(authorisation, caseData, task, caseDocuments);
        } else {
            return;
        }
        Party recipient = isDefendantPrint(task) ? caseData.getRespondent1() : caseData.getApplicant1();
        List<String> recipients = List.of(recipient.getPartyName());
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                     caseData.getLegacyCaseReference(), HEARING_PACK_LETTER_TYPE, recipients);
    }

    private boolean checkHearingDocumentAvailable(CaseData caseData) {
        return nonNull(caseData.getSystemGeneratedCaseDocuments())
            && !caseData.getSystemGeneratedCaseDocuments().isEmpty()
            && nonNull(caseData.getHearingDocuments())
            && !caseData.getHearingDocuments().isEmpty();
    }

    private boolean checkWelshHearingDocumentAvailable(CaseData caseData) {
        return nonNull(caseData.getSystemGeneratedCaseDocuments())
            && !caseData.getSystemGeneratedCaseDocuments().isEmpty()
            && nonNull(caseData.getHearingDocumentsWelsh())
            && !caseData.getHearingDocumentsWelsh().isEmpty();
    }

    private boolean isDefendantPrint(String task) {
        return task.equals(TASK_ID_DEFENDANT) || task.equals(TASK_ID_DEFENDANT_HMC);
    }

    private byte[] generateLetterContent(String authorisation, CaseData caseData, String task, CaseDocument... caseDocument) {
        byte[] letterContent;
        Party recipient = isDefendantPrint(task) ? caseData.getRespondent1() : caseData.getApplicant1();
        letterContent = coverLetterAppendService.makeDocumentMailable(caseData, authorisation, recipient, HEARING_NOTICE, caseDocument);
        return letterContent;
    }
}
