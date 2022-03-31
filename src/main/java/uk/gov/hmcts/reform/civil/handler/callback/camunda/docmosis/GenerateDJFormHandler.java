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
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentFormGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class GenerateDJFormHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.GENERATE_DJ_FORM,
        CaseEvent.GENERATE_DJ_FORM_SPEC
    );
    private static final String TASK_ID = "GenerateDJForm";
    private static final String TASK_ID_SPEC = "GenerateDJFormSpec";

    private final DefaultJudgmentFormGenerator defaultJudgmentFormGenerator;
    private final ObjectMapper objectMapper;

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
        return isSpecHandler(callbackParams) ? TASK_ID_SPEC : TASK_ID;
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && caseData.getDefendantDetails().getValue().getLabel().startsWith("Both")) {
            buildDocument(callbackParams, caseDataBuilder);
        } else if (ofNullable(caseData.getRespondent2()).isEmpty()) {
            buildDocument(callbackParams, caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void buildDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder caseDataBuilder) {
        List<CaseDocument> caseDocuments = defaultJudgmentFormGenerator.generate(
            callbackParams.getCaseData(),
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            callbackParams.getRequest().getEventId()
        );
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(caseDocuments.get(0)));
        if (caseDocuments.size() > 1) {
            systemGeneratedCaseDocuments.add(element(caseDocuments.get(1)));
        }
        caseDataBuilder.defaultJudgmentDocuments(systemGeneratedCaseDocuments);
    }

    private boolean isSpecHandler(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(CaseEvent.GENERATE_DJ_FORM_SPEC.name());
    }
}
