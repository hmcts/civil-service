package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UpholdingPreviousOrderReason;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.RequestReconsiderationGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DECISION_ON_RECONSIDERATION_REQUEST;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class JudgeDecisionOnReconsiderationRequestCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DECISION_ON_RECONSIDERATION_REQUEST);
    protected final ObjectMapper objectMapper;
    private final RequestReconsiderationGeneratorService requestReconsiderationGeneratorService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;
    private static final String CONFIRMATION_HEADER = "# Response has been submitted";
    private static final String CONFIRMATION_BODY_YES = "### Upholding previous order \n" +
        "A notification will be sent to the party applying for the request for reconsideration.";
    private static final String CONFIRMATION_BODY_CREATE_SDO = "### Amend previous order and create new SDO \n" +
        "A new SDO task has been created for this case and appears in 'Available tasks' on your dashboard. You will " +
        "need to go there to reselect the case to continue.";
    private static final String CONFIRMATION_BODY_CREATE_GENERAL_ORDER = "### Amend previous order and create a " +
        "general order" +
        " \n" +
        "To make a bespoke order in this claim, select 'General order' from the dropdown menu on the right of the " +
        "screen on your dashboard.";

    private static final String CONFIRMATION_BODY_CREATE_MAKE_AN_ORDER = "### Amend previous order and create a " +
        "general order" +
        " \n" +
        "To make a bespoke order in this claim, select 'Make an order' from the dropdown menu on the right of the " +
        "screen on your dashboard.";
    private static final String UPHOLDING_PREVIOUS_ORDER_REASON = "Having read the application for reconsideration of" +
        " " +
        "the Legal Advisor's order dated %s and the court file \n 1.The application for reconsideration of the order " +
        "is dismissed.";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::getUpholdingPreviousOrderReason)
            .put(callbackKey(MID, "generate-judge-decision-order"), this::generateJudgeDecisionOrder)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveRequestForReconsiderationReason)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse getUpholdingPreviousOrderReason(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();

        Optional<Element<CaseDocument>> sdoDocLatest = callbackParams.getCaseData().getSystemGeneratedCaseDocuments().stream().filter(
            caseDocumentElement -> caseDocumentElement.getValue().getDocumentType().equals(
                DocumentType.SDO_ORDER)).sorted(Comparator.comparing(
                    caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime(),
            Comparator.reverseOrder()
        )).findFirst();

        if (sdoDocLatest.isPresent()) {
            String sdoDate = formatLocalDateTime(sdoDocLatest.get().getValue().getCreatedDatetime(), DATE);
            caseDataBuilder.upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                             .reasonForReconsiderationTxtYes(String.format(
                                                                 UPHOLDING_PREVIOUS_ORDER_REASON,
                                                                 sdoDate
                                                             )).build());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse generateJudgeDecisionOrder(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        if (callbackParams.getCaseData().getDecisionOnRequestReconsiderationOptions().name().equals(DecisionOnRequestReconsiderationOptions.YES.name())) {
            CaseDocument requestForReconsiderationDocument = createDecisionOnReconsiderationDocmosisDoc(callbackParams, caseDataBuilder);
            assignCategoryId.assignCategoryIdToCaseDocument(requestForReconsiderationDocument, "ordersMadeOnApplications");
            caseDataBuilder.decisionOnReconsiderationDocument(requestForReconsiderationDocument);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse saveRequestForReconsiderationReason(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        if (!callbackParams.getCaseData().getDecisionOnRequestReconsiderationOptions().name().equals(DecisionOnRequestReconsiderationOptions.YES.name())) {
            caseDataBuilder.upholdingPreviousOrderReason(null);
        }

        if (callbackParams.getCaseData().getDecisionOnRequestReconsiderationOptions().name().equals(DecisionOnRequestReconsiderationOptions.YES.name())) {
            if (callbackParams.getCaseData().getDecisionOnReconsiderationDocument() != null) {
                CaseDocument requestForReconsiderationDocument =
                    callbackParams.getCaseData().getDecisionOnReconsiderationDocument();
                List<Element<CaseDocument>> systemGeneratedCaseDocuments =
                    callbackParams.getCaseData().getSystemGeneratedCaseDocuments();
                systemGeneratedCaseDocuments.add(element(requestForReconsiderationDocument));
                caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
                //delete temp so it will not show up twice in case file view
                caseDataBuilder.decisionOnReconsiderationDocument(null);
            }
            caseDataBuilder.businessProcess(BusinessProcess.ready(DECISION_ON_RECONSIDERATION_REQUEST));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseDocument createDecisionOnReconsiderationDocmosisDoc(CallbackParams callbackParams,
                                                            CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {

        return requestReconsiderationGeneratorService.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(getBody(callbackParams.getCaseData().getDecisionOnRequestReconsiderationOptions()))
            .build();
    }

    private String getBody(DecisionOnRequestReconsiderationOptions decisionOnRequestReconsiderationOptions) {
        switch (decisionOnRequestReconsiderationOptions) {
            case YES:
                return CONFIRMATION_BODY_YES;
            case CREATE_SDO:
                return CONFIRMATION_BODY_CREATE_SDO;
            default:
                return featureToggleService.isCaseProgressionEnabled()
                    ? CONFIRMATION_BODY_CREATE_MAKE_AN_ORDER
                    : CONFIRMATION_BODY_CREATE_GENERAL_ORDER;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
