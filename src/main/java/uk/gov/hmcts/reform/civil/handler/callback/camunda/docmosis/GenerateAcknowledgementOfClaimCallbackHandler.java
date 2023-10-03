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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.aos.AcknowledgementOfClaimGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_ACKNOWLEDGEMENT_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateAcknowledgementOfClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(GENERATE_ACKNOWLEDGEMENT_OF_CLAIM);
    private static final String TASK_ID = "AcknowledgeClaimGenerateAcknowledgementOfClaim";

    private final AcknowledgementOfClaimGenerator acknowledgementOfClaimGenerator;
    private final ObjectMapper objectMapper;
    private final AssignCategoryId assignCategoryId;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::prepareAcknowledgementOfClaim);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepareAcknowledgementOfClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        CaseDocument acknowledgementOfClaim = acknowledgementOfClaimGenerator.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(
                acknowledgementOfClaim, "");
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(acknowledgementOfClaim));
        systemGeneratedCaseDocuments.add(element(copy));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);

        assignCategoryId.assignCategoryIdToCaseDocument(acknowledgementOfClaim, "defendant1DefenseDirectionsQuestionnaire");
        assignCategoryId.assignCategoryIdToCaseDocument(copy, "DQRespondent");
        if (nonNull(caseData.getRespondent2DocumentGeneration()) && caseData.getRespondent2DocumentGeneration().equals("userRespondent2")) {
            assignCategoryId.assignCategoryIdToCaseDocument(acknowledgementOfClaim, "defendant2DefenseDirectionsQuestionnaire");
            assignCategoryId.assignCategoryIdToCaseDocument(copy, "DQRespondentTwo");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
