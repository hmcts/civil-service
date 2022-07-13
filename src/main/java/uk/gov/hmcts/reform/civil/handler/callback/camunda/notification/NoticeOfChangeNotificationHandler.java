package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT1_NOC;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeOfChangeNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NOTIFY_RESPONDENT1_NOC);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStart,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::submitted
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        System.out.println("ABOUT TO START!!!");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("ABOUT TO SUBMIT!!!");
        log.info(String.format("Notifying new representative: %s", caseData.getRespondentSolicitor1EmailAddress()));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private CallbackResponse submitted(CallbackParams callbackParams) {
        log.info("SUBMITTED!!!");
        return SubmittedCallbackResponse.builder()
            .build();
    }
}
