package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.CoverLetterService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_CLAIMANT_LIP_JBA_LETTER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGMENT_BY_ADMISSION_CLAIMANT;

@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
public class PostClaimantLipJbaLetterHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(POST_CLAIMANT_LIP_JBA_LETTER);
    private static final String JBA_LETTER_REF = "claimant_jba_letter";
    public static final String TASK_ID = "PostClaimantLipJBALetter";

    private final BulkPrintService bulkPrintService;
    private final CoverLetterService coverLetterGeneratorService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::postClaimantLipJbaLetter
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse postClaimantLipJbaLetter(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.isApplicantLiP()) {
            var claimantDJDoc = caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(document -> document.getValue().getDocumentType().equals(JUDGMENT_BY_ADMISSION_CLAIMANT))
                .toList().get(0);

            List<DocumentMetaData> documents = List.of(new DocumentMetaData(
                claimantDJDoc.getValue().getDocumentLink(),
                claimantDJDoc.getValue().getDocumentName(),
                LocalDate.now().toString()
            ));

            byte[] claimantDjLetterBinary = coverLetterGeneratorService.generateDocumentWithCoverLetterBinary(
                caseData.getApplicant1(), caseData, documents,
                "Claimant JBA letter.pdf",
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );
            List<String> recipients = List.of(caseData.getApplicant1().getPartyName());
            bulkPrintService.printLetter(
                claimantDjLetterBinary, caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(), JBA_LETTER_REF, recipients
            );
        }

        return emptyCallbackResponse(callbackParams);
    }
}
