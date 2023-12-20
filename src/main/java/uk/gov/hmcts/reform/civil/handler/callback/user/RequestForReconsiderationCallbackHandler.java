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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION;

@Service
@RequiredArgsConstructor
public class RequestForReconsiderationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_FOR_RECONSIDERATION);
    protected final ObjectMapper objectMapper;
    private static final String ERROR_MESSAGE_14_DAYS_ELAPSED
        = "You can no longer request a reconsideration because the deadline has expired";
    private static final String CONFIRMATION_HEADER = "# Your request has been submitted";
    private static final String CONFIRMATION_BODY = "### What happens next \n" +
        "You should receive an update on your request for determination after 10 days, please monitor" +
        " your notifications/dashboard for an update.";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::validateRequestEligibility)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveRequestForReconsiderationReason)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse validateRequestEligibility(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        Optional<Element<CaseDocument>> sdoDocLatest = callbackParams.getCaseData().getSystemGeneratedCaseDocuments()
            .stream().filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                .equals(DocumentType.SDO_ORDER))
            .sorted(Comparator.comparing(caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime(),
            Comparator.reverseOrder())).findFirst();
        if (sdoDocLatest.isPresent()) {
            LocalDateTime sdoDocLatestDate = sdoDocLatest.get().getValue().getCreatedDatetime();
            if (LocalDateTime.now().isAfter(sdoDocLatestDate.plusDays(14))) {
                errors.add(ERROR_MESSAGE_14_DAYS_ELAPSED);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse saveRequestForReconsiderationReason(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(CONFIRMATION_BODY)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
