package uk.gov.hmcts.reform.unspec.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.Time;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DISMISS_CLAIM;

@Service
@RequiredArgsConstructor
public class DismissClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(DISMISS_CLAIM);

    private final ObjectMapper objectMapper;
    private final Time time;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::updateBusinessStatusToReady
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private final CaseDetailsConverter caseDetailsConverter;

    private CallbackResponse updateBusinessStatusToReady(CallbackParams callbackParams) {
        CaseData data = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());

        CaseData.CaseDataBuilder caseDataBuilder = data.toBuilder()
            .businessProcess(BusinessProcess.ready(DISMISS_CLAIM))
            .claimDismissedDate(time.now());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
