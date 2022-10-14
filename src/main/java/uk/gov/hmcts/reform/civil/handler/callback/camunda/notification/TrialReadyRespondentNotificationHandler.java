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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.addTrialOrHearing;

@Service
@RequiredArgsConstructor
public class TrialReadyRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_TRIAL_READY
    );

    public static final String TASK_ID_RESPONDENT_ONE = "TrialReadyNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT_TWO = "TrialReadyNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE = "trial-ready-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondentSolicitorForTrialReady
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

    private CallbackResponse notifyRespondentSolicitorForTrialReady(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String respondentEmail =  isForRespondentSolicitor1(callbackParams)
            ? caseData.getRespondentSolicitor1EmailAddress()
            : caseData.getRespondentSolicitor2EmailAddress();

        if (null == respondentEmail && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
            respondentEmail = caseData.getRespondentSolicitor1EmailAddress();
        }

        notificationService.sendMail(
            respondentEmail,
            notificationsProperties.getSolicitorTrialReady(),
            isForRespondentSolicitor1(callbackParams) ? addPropertiesRep1(caseData) : addPropertiesRep2(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    public Map<String, String> addPropertiesRep2(CaseData caseData) {

        return Map.of(
            HEARING_OR_TRIAL, addTrialOrHearing(caseData),
            HEARING_DATE, caseData.getHearingDate().toString(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_DEFENDANT_REFERENCE, caseData.getSolicitorReferences().getRespondentSolicitor2Reference()
        );
    }

    public Map<String, String> addPropertiesRep1(CaseData caseData) {

        return Map.of(
            HEARING_OR_TRIAL, addTrialOrHearing(caseData),
            HEARING_DATE, caseData.getHearingDate().toString(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_DEFENDANT_REFERENCE, caseData.getSolicitorReferences().getRespondentSolicitor1Reference()
        );

    }

    private boolean isForRespondentSolicitor1(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_TRIAL_READY.name());
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return null;
    }
}
