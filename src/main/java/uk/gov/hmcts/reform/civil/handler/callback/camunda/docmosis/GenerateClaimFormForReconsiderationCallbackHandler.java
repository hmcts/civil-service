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
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.RequestReconsiderationGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_CLAIM_FORM_RECONSIDERATION;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateClaimFormForReconsiderationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_CLAIM_FORM_RECONSIDERATION);
    private static final String TASK_ID = "GenerateClaimFormForRecon";

    private final RequestReconsiderationGeneratorService requestReconsiderationGeneratorService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final AssignCategoryId assignCategoryId;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimFormForReconsideration);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse generateClaimFormForReconsideration(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        LocalDate issueDate = time.now().toLocalDate();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().issueDate(issueDate);
        CaseDocument requestForReconsiderationDocument = requestReconsiderationGeneratorService.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        if (requestForReconsiderationDocument != null) {
            updatedData.sdoOrderDocument(requestForReconsiderationDocument);
        }
        assignCategoryId.assignCategoryIdToCaseDocument(requestForReconsiderationDocument, "ordersMadeOnApplications");

        List<Element<CaseDocument>> systemGeneratedCaseDocuments =
            caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(requestForReconsiderationDocument));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();

    }

}
