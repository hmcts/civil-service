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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DirectionsQuestionnairePreparer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_QUESTIONNAIRE;

@Service
@RequiredArgsConstructor
public class GenerateDirectionsQuestionnaireCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GENERATE_DIRECTIONS_QUESTIONNAIRE
    );

    private static final List<String> TASK_IDS =
        Arrays.asList("ClaimantResponseGenerateDirectionsQuestionnaire",
            "DefendantResponseFullDefenceGenerateDirectionsQuestionnaire");

    private final ObjectMapper objectMapper;
    private final DirectionsQuestionnairePreparer directionsQuestionnairePreparer;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::prepareDirectionsQuestionnaire
        );
    }

    @Override
    public List<String> camundaActivityIds(CallbackParams callbackParams) {
        return TASK_IDS;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    /**
     * Next version for prepareDirectionsQuestionnaire.
     *
     * @param callbackParams parameters of the callback
     * @return response of the callback
     */
    private CallbackResponse prepareDirectionsQuestionnaire(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData = directionsQuestionnairePreparer.prepareDirectionsQuestionnaire(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper)).build();

    }

}
