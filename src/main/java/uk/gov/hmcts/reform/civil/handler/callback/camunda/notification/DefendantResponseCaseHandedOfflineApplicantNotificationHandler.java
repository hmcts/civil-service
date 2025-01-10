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
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant.CaseHandledOffLineApplicantSolicitorNotifierFactory;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE;

@Service
@RequiredArgsConstructor
public class DefendantResponseCaseHandedOfflineApplicantNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE);

    public static final String TASK_ID_APPLICANT1 = "DefendantResponseCaseHandedOfflineNotifyApplicantSolicitor1";

    private final CaseHandledOffLineApplicantSolicitorNotifierFactory caseHandledOffLineApplicantSolicitorNotifierFactory;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForCaseHandedOffline
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_APPLICANT1;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForCaseHandedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!caseData.isLipvLROneVOne()) {
            caseHandledOffLineApplicantSolicitorNotifierFactory.getCaseHandledOfflineSolicitorNotifier(caseData)
                .notifyApplicantSolicitorForCaseHandedOffline(caseData);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

}
