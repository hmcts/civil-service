package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_DETERMINATION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.judgment.JudgmentGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimLipResponseFormGenerator;

@Service
@RequiredArgsConstructor
public class GenerateJudgmentByDeterminationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GENERATE_JUDGMENT_BY_DETERMINATION
    );

    private final ObjectMapper objectMapper;
    private final JudgmentGenerator judgmentGenerator;

    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::generateJudgmentByDetermination
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateJudgmentByDetermination(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseDocument judgmentForm = judgmentGenerator.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        if (true) throw new RuntimeException("Dummy exception");
        CaseData updatedCaseData = caseData.toBuilder()
            .applicant1JudgmentByDeterminationForm(judgmentForm)
            .build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }
}
