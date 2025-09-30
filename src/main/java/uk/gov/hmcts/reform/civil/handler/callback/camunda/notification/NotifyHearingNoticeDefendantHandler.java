package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.service.HearingScheduledNotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_NOTICE_DEFENDANT;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyHearingNoticeDefendantHandler extends CallbackHandler {

    private final ObjectMapper objectMapper;
    private final HearingScheduledNotificationService hearingScheduledNotificationService;
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_HEARING_NOTICE_DEFENDANT
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyHearingNoticeToDefendant
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyHearingNoticeToDefendant(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        log.info("Notify hearing notice to claimant for case: {}", caseData.getCcdCaseReference());
        try {
            caseData = hearingScheduledNotificationService.sendNotificationForDefendant(caseData);
        } catch (NotificationException notificationException) {
            throw notificationException;
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
