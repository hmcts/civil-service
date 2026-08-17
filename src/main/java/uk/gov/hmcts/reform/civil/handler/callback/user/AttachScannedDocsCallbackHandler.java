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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Arrays;
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
        Map<String, Object> data = new HashMap<>();
        if (callbackParams.getRequest() != null && callbackParams.getRequest().getCaseDetails() != null
            && callbackParams.getRequest().getCaseDetails().getData() != null) {
            data.putAll(callbackParams.getRequest().getCaseDetails().getData());
        }

        if (hasPaperResponse(data, callbackParams.getCaseData())) {
            log.info("Paper response detected for caseId: {}, setting defendant responseMethod to OFFLINE",
                     callbackParams.getCaseData() != null ? callbackParams.getCaseData().getCcdCaseReference() : null);

            // Update CMC style respondents collection if present
            if (data.containsKey("respondents") && data.get("respondents") instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> respondents = (List<Map<String, Object>>) data.get("respondents");
                if (!respondents.isEmpty()) {
                    Map<String, Object> firstElement = new HashMap<>(respondents.get(0));
                    if (firstElement.get("value") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> valueMap = new HashMap<>((Map<String, Object>) firstElement.get("value"));
                        valueMap.put("responseMethod", "OFFLINE");
                        firstElement.put("value", valueMap);
                        respondents.set(0, firstElement);
                        data.put("respondents", respondents);
                    }
                }
            }

            if (isCivilCaseType(callbackParams)) {
                data.put("respondent1ResponseMethod", "OFFLINE");
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    private boolean isCivilCaseType(CallbackParams callbackParams) {
        return callbackParams.getRequest() != null
            && callbackParams.getRequest().getCaseDetails() != null
            && Objects.equals("CIVIL", callbackParams.getRequest().getCaseDetails().getCaseTypeId());
    }
}
