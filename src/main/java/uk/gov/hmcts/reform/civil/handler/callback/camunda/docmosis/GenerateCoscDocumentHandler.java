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
import uk.gov.hmcts.reform.civil.service.docmosis.cosc.CertificateOfDebtGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Service
@RequiredArgsConstructor
public class GenerateCoscDocumentHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.GENERATE_COSC_DOCUMENT
    );
    private static final String TASK_ID = "GenerateCoscDocument";

    private final CertificateOfDebtGenerator coscDocumentGenerartor;
    private final ObjectMapper objectMapper;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateCoscDocument);
    }

    private CallbackResponse generateCoscDocument(CallbackParams callbackParams) {
        CaseData caseDataInfo = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseDataInfo.toBuilder();
        buildCoscDocument(callbackParams, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void buildCoscDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseDocument caseDocument = coscDocumentGenerartor.generateDoc(
            callbackParams.getCaseData(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(caseDocument));
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }
}
