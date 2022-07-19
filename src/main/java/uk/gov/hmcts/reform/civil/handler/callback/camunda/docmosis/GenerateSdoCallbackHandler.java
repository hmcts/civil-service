package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenerateSdoCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CaseEvent.GENERATE_SDO);
    private static final String TASK_ID = "GenerateSdo";

    private final SdoGeneratorService sdoGeneratorService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::generateSdo);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams params) {
        return TASK_ID;
    }

    private CallbackResponse generateSdo(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseDocument document = sdoGeneratorService.generate(
            caseData,
            callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString()
        );
        List<Element<CaseDocument>> newDocuments = new ArrayList<>(caseData.getSystemGeneratedCaseDocuments());
        newDocuments.add(ElementUtils.element(document));
        caseData = caseData.toBuilder()
            .systemGeneratedCaseDocuments(newDocuments)
            .build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
