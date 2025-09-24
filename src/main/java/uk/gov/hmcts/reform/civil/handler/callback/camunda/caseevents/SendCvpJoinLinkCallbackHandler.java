package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_CVP_JOIN_LINK;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendCvpJoinLinkCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_CVP_JOIN_LINK);

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::updateSchedulerFlag
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateSchedulerFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData caseDataUpdated = caseData.toBuilder()
            .cvpLinkSchedulerProcessed(YesOrNo.YES)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated.toMap(objectMapper))
                .build();
    }
}
