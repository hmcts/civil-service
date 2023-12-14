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
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class MediationSuccessfulRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL);
    private static final String REFERENCE_TEMPLATE = "mediation-successful-respondent-notification-%s";
    public static final String TASK_ID = "MediationSuccessfulNotifyRespondent";
    private final Map<String, Callback> callbacksMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyRespondent
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbacksMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyRespondent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.isRespondent1NotRepresented() && caseData.getRespondent1().getPartyEmail() != null) {
            notificationService.sendMail(
                caseData.getRespondent1().getPartyEmail(),
                addTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference()));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();

    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    private String addTemplate(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplateWelsh() :
            notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplate();
    }

}
