package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ATTACH_SCANNED_DOCS;
import static uk.gov.hmcts.reform.civil.utils.MapperUtil.hasPaperResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachScannedDocsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ATTACH_SCANNED_DOCS);
    private static final String RESPONDENTS = "respondents";
    private static final String VALUE = "value";
    private static final String RESPONSE_METHOD = "responseMethod";
    private static final String RESPONDENT_1_RESPONSE_METHOD = "respondent1ResponseMethod";
    private static final String OFFLINE = "OFFLINE";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::setDefendantResponseMethod
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse setDefendantResponseMethod(CallbackParams callbackParams) {
        Map<String, Object> data = getCallbackData(callbackParams);

        if (!hasPaperResponse(data, callbackParams.getCaseData())) {
            return buildResponse(data);
        }

        log.info("Paper response detected for caseId: {}, setting defendant responseMethod to OFFLINE",
                 callbackParams.getCaseData().getCcdCaseReference());

        updateFirstRespondentResponseMethod(data);

        if (callbackParams.isCivilCaseType()) {
            data.put(RESPONDENT_1_RESPONSE_METHOD, OFFLINE);
        }

        return buildResponse(data);
    }

    private Map<String, Object> getCallbackData(CallbackParams callbackParams) {
        Map<String, Object> data = new HashMap<>();

        if (callbackParams.getRequest() == null
            || callbackParams.getRequest().getCaseDetails() == null
            || callbackParams.getRequest().getCaseDetails().getData() == null) {
            return data;
        }

        data.putAll(callbackParams.getRequest().getCaseDetails().getData());
        return data;
    }

    private void updateFirstRespondentResponseMethod(Map<String, Object> data) {
        if (!(data.get(RESPONDENTS) instanceof List<?> respondents) || respondents.isEmpty()) {
            return;
        }

        Object firstRespondent = respondents.getFirst();
        if (!(firstRespondent instanceof Map<?, ?> firstRespondentMap)
            || !(firstRespondentMap.get(VALUE) instanceof Map<?, ?> valueMap)) {
            return;
        }

        Map<String, Object> updatedValue = new HashMap<>((Map<String, Object>) valueMap);
        updatedValue.put(RESPONSE_METHOD, OFFLINE);

        Map<String, Object> updatedRespondent = new HashMap<>((Map<String, Object>) firstRespondentMap);
        updatedRespondent.put(VALUE, updatedValue);

        ((List<Object>) respondents).set(0, updatedRespondent);
        data.put(RESPONDENTS, respondents);
    }

    private CallbackResponse buildResponse(Map<String, Object> data) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }
}
