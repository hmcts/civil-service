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
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.aos.AcknowledgementOfClaimGeneratorForSpec;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class GenerateAcknowledgementOfClaimForSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(GENERATE_ACKNOWLEDGEMENT_OF_CLAIM_SPEC);
    private static final String TASK_ID = "AcknowledgeClaimGenerateAcknowledgementOfClaimForSpec";

    private final AcknowledgementOfClaimGeneratorForSpec acknowledgementOfClaimGenerator;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;

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
        if (toggleService.isLrSpecEnabled()) {
            return EVENTS;
        } else {
            return Collections.emptyList();
        }
    }

    private CallbackResponse prepareAcknowledgementOfClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        CaseDocument acknowledgementOfClaim = acknowledgementOfClaimGenerator.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(acknowledgementOfClaim));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
