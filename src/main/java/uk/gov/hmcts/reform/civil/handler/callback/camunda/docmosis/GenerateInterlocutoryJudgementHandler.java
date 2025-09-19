package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse.InterlocutoryJudgementDocGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class GenerateInterlocutoryJudgementHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.GENERATE_INTERLOCUTORY_JUDGEMENT_DOCUMENT);
    private final ObjectMapper objectMapper;
    private final InterlocutoryJudgementDocGenerator interlocutoryJudgementDocGenerator;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final FeatureToggleService featureToggleService;
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT),
        this::generateInterlocutoryJudgementDoc
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateInterlocutoryJudgementDoc(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isGenerateInterlocutoryDocNotPermitted(callbackParams)) {
            return SubmittedCallbackResponse.builder().build();
        }

        CaseDocument interlocutoryJudgementDoc = interlocutoryJudgementDocGenerator.generateInterlocutoryJudgementDoc(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        CaseData updatedCaseData;
        if ((caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
            List<Element<CaseDocument>> preTranslationDocuments = caseData.getPreTranslationDocuments();
            preTranslationDocuments.add(element(interlocutoryJudgementDoc));
            updatedCaseData = caseData.toBuilder()
                .preTranslationDocuments(preTranslationDocuments)
                .preTranslationDocumentType(PreTranslationDocumentType.INTERLOCUTORY_JUDGMENT)
                .build();
        } else {
            updatedCaseData = caseData.toBuilder()
                .systemGeneratedCaseDocuments(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
                    interlocutoryJudgementDoc,
                    caseData
                ))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private boolean hasClaimantRejectedCourtDecision(CaseData caseData) {
        return Optional.ofNullable(caseData)
            .map(CaseData::getCaseDataLiP)
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasClaimantRejectedCourtDecision)
            .isPresent();
    }

    private boolean isGenerateInterlocutoryDocNotPermitted(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isCompanyOROrganisation = caseData.getRespondent1().isCompanyOROrganisation();
        return isCompanyOROrganisation || !hasClaimantRejectedCourtDecision(caseData);
    }
}
