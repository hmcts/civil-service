package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceNotificationType;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceSolicitorNotifier;
import uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence.FullDefenceSolicitorNotifierFactory;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC;

@Service
@RequiredArgsConstructor
public class DefendantResponseApplicantNotificationHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE,
        NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC
    );

    public static final String TASK_ID = "DefendantResponseFullDefenceNotifyApplicantSolicitor1";
    public static final String TASK_ID_CC = "DefendantResponseFullDefenceNotifyRespondentSolicitor1CC";
    public static final String TASK_ID_CC_RESP1 = "DefendantResponseFullDefenceNotifyRespondentSolicitor1";
    public static final String TASK_ID_CC_RESP2 = "DefendantResponseFullDefenceNotifyRespondentSolicitor2CC";

    private final FullDefenceSolicitorNotifierFactory fullDefenceSolicitorNotifierFactory;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifySolicitorsForDefendantResponse
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        switch (caseEvent) {
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE:
                return TASK_ID;
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                return TASK_ID_CC;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                return TASK_ID_CC_RESP1;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC:
                return TASK_ID_CC_RESP2;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifySolicitorsForDefendantResponse(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());

        FullDefenceSolicitorNotifier fullDefenceSolicitorNotifier;

        switch (caseEvent) {
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE:
                fullDefenceSolicitorNotifier = fullDefenceSolicitorNotifierFactory.getNotifier(FullDefenceNotificationType.APPLICANT_SOLICITOR_ONE,
                    caseData.getCaseAccessCategory());
                break;
            case NOTIFY_APPLICANT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                fullDefenceSolicitorNotifier = fullDefenceSolicitorNotifierFactory.getNotifier(FullDefenceNotificationType.APPLICANT_SOLICITOR_ONE_CC,
                    caseData.getCaseAccessCategory());
                break;
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_DEFENDANT_RESPONSE_CC:
                fullDefenceSolicitorNotifier =
                    fullDefenceSolicitorNotifierFactory.getNotifier(FullDefenceNotificationType.RESPONDENT_SOLICITOR_ONE_CC,
                        caseData.getCaseAccessCategory());
                break;
            case NOTIFY_RESPONDENT_SOLICITOR2_FOR_DEFENDANT_RESPONSE_CC:
                fullDefenceSolicitorNotifier =
                    fullDefenceSolicitorNotifierFactory.getNotifier(FullDefenceNotificationType.RESPONDENT_SOLICITOR_TWO_CC,
                        caseData.getCaseAccessCategory());
                break;
            default:
                throw new CallbackException(String.format("Callback handler received illegal event: %s", caseEvent));
        }

        fullDefenceSolicitorNotifier.notifySolicitorForDefendantResponse(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}
