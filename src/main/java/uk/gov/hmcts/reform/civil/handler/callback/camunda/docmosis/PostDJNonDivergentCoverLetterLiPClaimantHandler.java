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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_DJ_NON_DIVERGENT_COVER_LETTER_CLAIMANT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_CLAIMANT1;

@SuppressWarnings("unchecked")
@Service
@RequiredArgsConstructor
public class PostDJNonDivergentCoverLetterLiPClaimantHandler extends CallbackHandler {

    private static final String COVER_LETTER_REF = "default-judgment-non-divergent-spec-cover_letter";
    private static final List<CaseEvent> EVENTS = List.of(POST_DJ_NON_DIVERGENT_COVER_LETTER_CLAIMANT);
    public static final String TASK_ID = "PostClaimantDJCoverLetterAndDocument";
    private final BulkPrintService bulkPrintService;
    private final CoverLetterService coverLetterGeneratorService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::postCoverLetterToLiPClaimant
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

    private CallbackResponse postCoverLetterToLiPClaimant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.isApplicantLiP()) {
            var claimantDJDoc = caseData.getDefaultJudgmentDocuments().stream()
                .filter(document -> document.getValue().getDocumentType().equals(DEFAULT_JUDGMENT_CLAIMANT1))
                .toList().get(0);

            List<DocumentMetaData> documents = List.of(new DocumentMetaData(
                claimantDJDoc.getValue().getDocumentLink(),
                claimantDJDoc.getValue().getDocumentName(),
                LocalDate.now().toString()
            ));

            byte[] claimantDjLetterBinary = coverLetterGeneratorService.generateDocumentWithCoverLetterBinary(
                caseData.getApplicant1(), caseData, documents,
                "Claimant DJ letter.pdf",
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );
            List<String> recipients = List.of(caseData.getApplicant1().getPartyName());
            bulkPrintService.printLetter(
                claimantDjLetterBinary, caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(), COVER_LETTER_REF, recipients
            );
        }

        return emptyCallbackResponse(callbackParams);
    }
}
