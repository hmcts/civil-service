package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification;

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
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.HearingScheduledNotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationException;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_HEARING_NOTICE_DEFENDANT;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyHearingNoticeDefendantHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

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

        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
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
