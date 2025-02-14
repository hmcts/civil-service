package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.civil.service.docmosis.dj.nondivergent.NonDivergentSpecDefaultJudgmentFormGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class GenerateDJFormHandlerSpecNonDivergent extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GEN_DJ_FORM_NON_DIVERGENT_SPEC_DEFENDANT,
        GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT
    );
    private static final String TASK_ID_CLAIMANT = "GenerateDJFormNondivergentSpecClaimant";
    private static final String TASK_ID_DEFENDANT = "GenerateDJFormNondivergentSpecDefendant";

    private final NonDivergentSpecDefaultJudgmentFormGenerator defaultJudgmentFormGenerator;
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
        return isClaimantEvent(callbackParams) ? TASK_ID_CLAIMANT  : TASK_ID_DEFENDANT;
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        if (!caseData.isLipvLipOneVOne()){
            log.info("Case is not lipvliponevone, generating default judgment form");
            buildDocument(callbackParams, caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void buildDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder caseDataBuilder) {
        List<CaseDocument> caseDocuments = defaultJudgmentFormGenerator.generateNonDivergentDocs(
            callbackParams.getCaseData(),
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            callbackParams.getRequest().getEventId()
        );
        List<Element<CaseDocument>> systemGeneratedCaseDocuments;
        if (callbackParams.getCaseData().getDefaultJudgmentDocuments() != null) {
            systemGeneratedCaseDocuments = callbackParams.getCaseData().getDefaultJudgmentDocuments();
        } else {
            systemGeneratedCaseDocuments = new ArrayList<>();
        }
        caseDocuments.forEach(caseDocument ->
            systemGeneratedCaseDocuments.add(element(caseDocument))
        );
        caseDataBuilder.defaultJudgmentDocuments(systemGeneratedCaseDocuments);
    }

    private boolean isClaimantEvent(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(CaseEvent.GEN_DJ_FORM_NON_DIVERGENT_SPEC_CLAIMANT.name());
    }
}
