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
import uk.gov.hmcts.reform.civil.service.SystemGeneratedDocumentService;
import uk.gov.hmcts.reform.civil.service.docmosis.settlementagreement.SettlementAgreementFormGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM;

@Service
@RequiredArgsConstructor
public class GenerateSettlementAgreementFormCallbackHandler extends CallbackHandler {

    private final SettlementAgreementFormGenerator settlementAgreementFormGenerator;
    private final SystemGeneratedDocumentService systemGeneratedDocumentService;
    private final ObjectMapper objectMapper;
    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM);
    private final Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateResponseDocument);
    private static final String TASK_ID = "GenerateSignSettlementAgreement";

    private CallbackResponse generateResponseDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseDataBuilder = caseData.toBuilder();

        if (!caseData.getRespondent1().isIndividualORSoleTrader()) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        CaseDocument claimantResponseForm = settlementAgreementFormGenerator.generate(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        updatedCaseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedDocumentService.getSystemGeneratedDocumentsWithAddedDocument(
                claimantResponseForm,
                caseData
        ));

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }
}
