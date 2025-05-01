package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.CaseHandledOfflineRecipient;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.respondent.CaseHandledOffLineRespondentSolicitorNotifierFactory;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isRespondent1;

@Service
@RequiredArgsConstructor
public class DefendantResponseCaseHandedOfflineRespondentNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE
    );
    public static final String TASK_ID_RESPONDENT1 = "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT2 = "DefendantResponseCaseHandedOfflineNotifyRespondentSolicitor2";

    private final CaseHandledOffLineRespondentSolicitorNotifierFactory caseHandledOffLineRespondentSolicitorNotifierFactory;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForCaseHandedOffline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent1(
            callbackParams,
            NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE
        ) ? TASK_ID_RESPONDENT1
            : TASK_ID_RESPONDENT2;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    //Offline notification will point to a new MP template for displaying defendant responses
    private CallbackResponse notifyRespondentSolicitorForCaseHandedOffline(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData caseData = callbackParams.getCaseData();
        caseHandledOffLineRespondentSolicitorNotifierFactory.getCaseHandledOfflineSolicitorNotifier(caseData)
            .notifyRespondentSolicitorForCaseHandedOffline(caseData,
                NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE.equals(caseEvent)
                    ? CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR1
                    : CaseHandledOfflineRecipient.RESPONDENT_SOLICITOR2);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

}
