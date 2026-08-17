package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ATTACH_SCANNED_DOCS;
import static uk.gov.hmcts.reform.civil.utils.MapperUtil.hasPaperResponse;

@Slf4j
@Service
public class AttachScannedDocsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ATTACH_SCANNED_DOCS);
    private static final String RESPONDENTS = "respondents";
    private static final String VALUE = "value";
    private static final String RESPONSE_METHOD = "responseMethod";
    private static final String RESPONDENT_1_RESPONSE_METHOD = "respondent1ResponseMethod";
    private static final String OFFLINE = "OFFLINE";

    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public AttachScannedDocsCallbackHandler(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

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

        CaseData caseData = geCaseData(callbackParams);
        if (hasPaperResponse(caseData)) {
            log.info("Paper response detected for caseId: {}, setting defendant responseMethod to OFFLINE",
                     callbackParams.getCaseData().getCcdCaseReference());

            updateFirstRespondentResponseMethod(data);

            if (callbackParams.isCivilCaseType()) {
                data.put(RESPONDENT_1_RESPONSE_METHOD, OFFLINE);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.convertToMap(caseData))
            .build();
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

    private CaseData geCaseData(CallbackParams callbackParams) {
        if (callbackParams.getRequest() == null
            || callbackParams.getRequest().getCaseDetails() == null
            || callbackParams.getRequest().getCaseDetails().getData() == null) {
            return CaseData.builder().build();
        }
        return caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
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

        Map<String, Object> updatedValue = toStringObjectMap(valueMap);
        updatedValue.put(RESPONSE_METHOD, OFFLINE);

        Map<String, Object> updatedRespondent = toStringObjectMap(firstRespondentMap);
        updatedRespondent.put(VALUE, updatedValue);

        List<Object> updatedRespondents = new ArrayList<>(respondents);
        updatedRespondents.set(0, updatedRespondent);

        data.put(RESPONDENTS, updatedRespondents);
    }

    private Map<String, Object> toStringObjectMap(Map<?, ?> source) {
        Map<String, Object> result = new HashMap<>();

        source.forEach((key, value) -> {
            if (key instanceof String stringKey) {
                result.put(stringKey, value);
            }
        });

        return result;
    }
}
