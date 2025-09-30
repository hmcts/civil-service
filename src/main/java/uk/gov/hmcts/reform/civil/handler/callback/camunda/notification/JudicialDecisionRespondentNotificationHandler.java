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
import uk.gov.hmcts.reform.civil.service.JudicialNotificationService;
import uk.gov.hmcts.reform.civil.service.NotificationException;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudicialDecisionRespondentNotificationHandler extends CallbackHandler {

    private static final String SOLICITOR_TYPE = "respondent";
    private final ObjectMapper objectMapper;
    private final JudicialNotificationService judicialNotificationService;
    private static final List<CaseEvent> EVENTS = List.of(
        START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::judicialDecisionNotification
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse judicialDecisionNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Judicial decision respondent notification for case: {}", caseData.getCcdCaseReference());
        try {
            caseData = judicialNotificationService.sendNotification(caseData, SOLICITOR_TYPE);
        } catch (NotificationException notificationException) {
            throw notificationException;
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
