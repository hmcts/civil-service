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

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_RECONSIDERATION_UPHELD;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UpholdingPreviousOrderReason;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DECISION_ON_RECONSIDERATION_REQUEST;

@Service
@RequiredArgsConstructor
public class JudgeDecisionOnReconsiderationRequestCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DECISION_ON_RECONSIDERATION_REQUEST);
    protected final ObjectMapper objectMapper;
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
    private static final String upholdingPreviousOrderReason = "Having read the application for reconsideration of " +
        "the Legal Advisor's order dated %s and the court file \n 1.The application for reconsideration of the order " +
        "is dismissed.";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::getUpholdingPreviousOrderReason)
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

        String sdoDate = formatLocalDateTime(sdoDocLatest.get().getValue().getCreatedDatetime(), DATE);
        caseDataBuilder.upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                         .reasonForReconsiderationTxtYes(String.format(upholdingPreviousOrderReason, sdoDate)).build());
        if (sdoDocLatest.isPresent()) {
            String sdoDate = formatLocalDateTime(sdoDocLatest.get().getValue().getCreatedDatetime(), DATE);
            caseDataBuilder.upholdingPreviousOrderReason(UpholdingPreviousOrderReason.builder()
                                                             .reasonForReconsiderationTxtYes(String.format(
                                                                 upholdingPreviousOrderReason,
                                                                 sdoDate
                                                             )).build());
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

        if(callbackParams.getCaseData().getDecisionOnRequestReconsiderationOptions().name().equals(DecisionOnRequestReconsiderationOptions.YES.name())){
            caseDataBuilder.businessProcess(BusinessProcess.ready(NOTIFY_CLAIM_RECONSIDERATION_UPHELD));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(getBody(callbackParams.getCaseData().getDecisionOnRequestReconsiderationOptions()))
            .build();
    }

    private String getBody(DecisionOnRequestReconsiderationOptions decisionOnRequestReconsiderationOptions) {
        if (decisionOnRequestReconsiderationOptions.equals(DecisionOnRequestReconsiderationOptions.YES)) {
            return CONFIRMATION_BODY_YES;
        } else if (decisionOnRequestReconsiderationOptions.equals(DecisionOnRequestReconsiderationOptions.CREATE_SDO)) {
            return CONFIRMATION_BODY_CREATE_SDO;
        } else {
            return CONFIRMATION_BODY_CREATE_GENERAL_ORDER;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
