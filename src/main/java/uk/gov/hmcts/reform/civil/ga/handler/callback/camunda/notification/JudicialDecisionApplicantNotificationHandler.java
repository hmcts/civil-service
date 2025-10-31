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
import uk.gov.hmcts.reform.civil.ga.service.JudicialNotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationException;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudicialDecisionApplicantNotificationHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final String SOLICITOR_TYPE = "applicant";

    private final ObjectMapper objectMapper;
    private final JudicialNotificationService judicialNotificationService;
    private static final List<CaseEvent> EVENTS = List.of(
        START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION
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
        GeneralApplicationCaseData caseData = (GeneralApplicationCaseData) callbackParams.getBaseCaseData();
        log.info("Judicial decision applicant notification for case: {}", caseData.getCcdCaseReference());
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
