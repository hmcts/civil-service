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
import uk.gov.hmcts.reform.civil.service.docmosis.draft.DraftClaimFormGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DRAFT_FORM;

@Service
@RequiredArgsConstructor
public class GenerateDraftClaimFormCallBackHandler extends CallbackHandler {

    private static List<CaseEvent> EVENTS = List.of(GENERATE_DRAFT_FORM);
    private final Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateDraftPdfForm);
    private final ObjectMapper objectMapper;
    private DraftClaimFormGenerator draftClaimFormGenerator;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateDraftPdfForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        draftClaimFormGenerator.generate(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

}
