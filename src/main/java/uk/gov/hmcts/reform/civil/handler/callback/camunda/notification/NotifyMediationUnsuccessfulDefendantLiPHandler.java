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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP;

@Service
@RequiredArgsConstructor
public class NotifyMediationUnsuccessfulDefendantLiPHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    private static final String LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP = "notification-mediation-unsuccessful-defenant-LIP-%s";
    private static final String TASK_ID_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP = "SendMediationUnsuccessfulDefendantLIP";

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP);
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantLiPForMediationUnsuccessful
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(DEFENDANT_NAME, caseData.getRespondent1().getPartyName(),
                      CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                      CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP;
    }

    private CallbackResponse notifyDefendantLiPForMediationUnsuccessful(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        sendEmail(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private void sendEmail(final CaseData caseData) {
        if (caseData.getRespondent1().getPartyEmail() != null) {
            String recipient = caseData.getRespondent1().getPartyEmail();
            notificationService.sendMail(
                recipient,
                addTemplate(caseData),
                addProperties(caseData),
                String.format(LOG_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP, caseData.getLegacyCaseReference())
            );
        }
    }

    private String addTemplate(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getMediationUnsuccessfulDefendantLIPBilingualTemplate()
            : notificationsProperties.getMediationUnsuccessfulDefendantLIPTemplate();
    }
}
