package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.caseProgression.JudgeFinalOrderGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

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
    public static final String HEADER = "## Your order has been issued \n ### Case number \n ### #%s";
    public static final String BODY_1v1 = "The order has been sent to: \n ### Claimant 1 \n %s \n ### Defendant 1 \n %s";
    public static final String BODY_2v1 = "The order has been sent to: \n ### Claimant 1 \n %s \n ### Claimant 2 \n %s"
        + "\n ### Defendant 1 \n %s";
    public static final String BODY_1v2 = "The order has been sent to: \n ### Claimant 1 \n %s \n ### Defendant 1 \n %s"
        + "\n ### Defendant 2 \n %s";
    private final ObjectMapper objectMapper;
    private final JudgeFinalOrderGenerator judgeFinalOrderGenerator;
    private static  CaseDocument finalDocument;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "populate-freeForm-values"), this::populateFreeFormValues,
            callbackKey(MID, "generate-document-preview"), this::generatePreviewDocument,
            callbackKey(ABOUT_TO_SUBMIT), this::addGeneratedDocumentToCollection,
            callbackKey(SUBMITTED), this::buildConfirmation
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

    private CallbackResponse generatePreviewDocument(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        CaseDocument finalDocument = judgeFinalOrderGenerator.generate(
            caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());

        if (caseData.getFinalOrderSelection() == FinalOrderSelection.FREE_FORM_ORDER) {
            caseDataBuilder.freeFormOrderDocument(finalDocument.getDocumentLink());
        } else {
            caseDataBuilder.assistedOrderDocument(finalDocument.getDocumentLink());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse addGeneratedDocumentToCollection(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        CaseDocument finalDocument = judgeFinalOrderGenerator.generate(
            caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
        finalCaseDocuments.add(element(finalDocument));
        if (!isEmpty(caseData.getFinalOrderDocumentCollection())) {
            finalCaseDocuments.addAll(caseData.getFinalOrderDocumentCollection());
        }

        caseDataBuilder.finalOrderDocumentCollection(finalCaseDocuments);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format(HEADER, caseData.getCcdCaseReference());
    }

    private String getBody(CaseData caseData) {
        if (caseData.getRespondent2() != null) {
            return format(BODY_1v2, caseData.getApplicant1().getPartyName(), caseData.getRespondent1().getPartyName(),
                          caseData.getRespondent2().getPartyName());
        }
        if ((caseData.getApplicant2() != null)) {
            return format(BODY_2v1, caseData.getApplicant1().getPartyName(), caseData.getApplicant2().getPartyName(),
                          caseData.getRespondent1().getPartyName());
        }
        else {
            return format(BODY_1v1, caseData.getApplicant1().getPartyName(), caseData.getRespondent1().getPartyName());
        }
    }

}
