package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.JudgmentByDeterminationDocGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateJudgmentByDeterminationFormHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT,
        GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT
    );
    private static final String TASK_ID_CLAIMANT = "GenerateClaimantJudgmentByDeterminationDoc";
    private static final String TASK_ID_DEFENDANT = "GenerateDefendantJudgmentByDeterminationDoc";
    private final AssignCategoryId assignCategoryId;
    private final ObjectMapper objectMapper;
    private final JudgmentByDeterminationDocGenerator judgmentByDeterminationDocGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isClaimantEvent(callbackParams) ? TASK_ID_CLAIMANT  : TASK_ID_DEFENDANT;
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        buildDocument(callbackParams, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void buildDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        List<CaseDocument> caseDocuments = judgmentByDeterminationDocGenerator.generateDocs(
            callbackParams.getCaseData(),
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            callbackParams.getRequest().getEventId()
        );
        List<Element<CaseDocument>> systemGeneratedCaseDocuments;
        if (callbackParams.getCaseData().getSystemGeneratedCaseDocuments() != null) {
            systemGeneratedCaseDocuments = callbackParams.getCaseData().getSystemGeneratedCaseDocuments();
        } else {
            systemGeneratedCaseDocuments = new ArrayList<>();
        }
        caseDocuments.forEach(caseDocument ->
            systemGeneratedCaseDocuments.add(element(caseDocument))
        );
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
    }

    private boolean isClaimantEvent(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT.name());
    }
}
