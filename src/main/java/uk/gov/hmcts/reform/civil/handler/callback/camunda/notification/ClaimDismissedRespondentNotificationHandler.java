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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isRespondent1;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@Service
@RequiredArgsConstructor
public class ClaimDismissedRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_DISMISSED
    );
    public static final String TASK_ID_RESPONDENT1 = "ClaimDismissedNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT2 = "ClaimDismissedNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE =
        "claim-dismissed-respondent-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyRespondentSolicitorForClaimDismissed
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED) ? TASK_ID_RESPONDENT1
            : TASK_ID_RESPONDENT2;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForClaimDismissed(
        CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = !is1v1Or2v1Case(caseData)
            && !isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_DISMISSED) ? caseData
                .getRespondentSolicitor2EmailAddress() : caseData.getRespondentSolicitor1EmailAddress();

        notificationService.sendMail(
            recipient,
            notificationsProperties.getSolicitorClaimDismissed(),
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
}
