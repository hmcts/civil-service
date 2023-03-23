package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.caseProgression.JudgeFinalOrderGenerator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;

@Service
@RequiredArgsConstructor
public class GenerateDirectionOrderCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(GENERATE_DIRECTIONS_ORDER);
    private static final String ON_INITIATIVE_SELECTION_TEST = "As this order was made on the court's own initiative "
        + "any party affected by the order may apply to set aside, vary or stay the order. Any such application must "
        + "be made by 4pm on";
    private static final String WITHOUT_NOTICE_SELECTION_TEXT = "If you were not notified of the application before "
        + "this order was made, you may apply to set aside, vary or stay the order. Any such application must be made "
        + "by 4pm on";
    private final ObjectMapper objectMapper;
    private final JudgeFinalOrderGenerator judgeFinalOrderGenerator;


    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "populate-freeForm-values"), this::populateFreeFormValues,
            callbackKey(MID, "generate-document-preview"), this::generateDocument,
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateFreeFormValues(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        caseDataBuilder.orderOnCourtInitiative(FreeFormOrderValues.builder()
                                                   .onInitiativeSelectionTextArea(ON_INITIATIVE_SELECTION_TEST)
                                                   .onInitiativeSelectionDate(LocalDate.now())
                                                   .build());
        caseDataBuilder.orderWithoutNotice(FreeFormOrderValues.builder()
                                                   .withoutNoticeSelectionTextArea(WITHOUT_NOTICE_SELECTION_TEXT)
                                                   .withoutNoticeSelectionDate(LocalDate.now())
                                                   .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        CaseDocument document = judgeFinalOrderGenerator.generate(
            caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());

        if (caseData.getFinalOrderSelection().equals(FinalOrderSelection.FREE_FORM_ORDER)) {
            caseDataBuilder.freeFormOrderDocument(document.getDocumentLink());
        } else {
            caseDataBuilder.assistedOrderDocument(document.getDocumentLink());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

}
