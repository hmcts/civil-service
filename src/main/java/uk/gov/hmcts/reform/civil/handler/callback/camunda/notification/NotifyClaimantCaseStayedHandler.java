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
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_STAY_CASE;

@Service
@RequiredArgsConstructor
public class NotifyClaimantCaseStayedHandler extends CallbackHandler implements NotificationData {

    private static final String TASK_ID = "NotifyClaimantStayCase";
    private static final String REFERENCE_TEMPLATE = "case-stayed-claimant-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_STAY_CASE);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotification);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    public CallbackResponse sendNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getRecipientEmail(caseData),
            getNotificationTemplate(caseData),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getNotificationTemplate(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return caseData.isClaimantBilingual()
                ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getNotifyLRCaseStayed();
        }

    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        );
    }

    private String getRecipientEmail(CaseData caseData) {
        return caseData.isApplicantLiP()
            ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }
}
