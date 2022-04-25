package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@Service
@RequiredArgsConstructor
public class CaseTakenOfflineRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_TAKEN_OFFLINE
    );

    public static final String TASK_ID_RESPONDENT_ONE = "TakeCaseOfflineNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT_TWO = "TakeCaseOfflineNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE = "case-taken-offline-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForCaseTakenOffline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isForRespondentSolicitor1(callbackParams) ? TASK_ID_RESPONDENT_ONE : TASK_ID_RESPONDENT_TWO;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForCaseTakenOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String respondentEmail =  isForRespondentSolicitor1(callbackParams)
            ? caseData.getRespondentSolicitor1EmailAddress()
            : caseData.getRespondentSolicitor2EmailAddress();

        notificationService.sendMail(
            respondentEmail,
            notificationsProperties.getSolicitorCaseTakenOffline(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }

    private boolean isForRespondentSolicitor1(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_TAKEN_OFFLINE.name());
    }
}
