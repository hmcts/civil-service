package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

@RequiredArgsConstructor
public abstract class CaseDismissNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.NOTIFY_CLAIMANT_DISMISS_CASE,
        CaseEvent.NOTIFY_DEFENDANT_DISMISS_CASE
    );

    private static final String REFERENCE_TEMPLATE_CLAIMANT = "dismiss-case-claimant-notification-%s";
    private static final String REFERENCE_TEMPLATE_DEFENDANT = "dismiss-case-defendant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendNotification);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, getPartyName(caseData),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData)
        );
    }

    protected abstract String getPartyName(CaseData caseData);

    public CallbackResponse sendNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getRecipient(callbackParams),
            getNotificationTemplate(callbackParams),
            addProperties(caseData),
            String.format(getReferenceTemplate(callbackParams), caseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getReferenceTemplate(CallbackParams params) {
        if (CaseEvent.NOTIFY_CLAIMANT_DISMISS_CASE.name().equals(params.getRequest().getEventId())) {
            return REFERENCE_TEMPLATE_CLAIMANT;
        } else {
            return REFERENCE_TEMPLATE_DEFENDANT;
        }
    }

    private String getRecipient(CallbackParams params) {
        CaseData caseData = params.getCaseData();
        if (CaseEvent.NOTIFY_CLAIMANT_DISMISS_CASE.name().equals(params.getRequest().getEventId())) {
            return caseData.isApplicantLiP()
                ? caseData.getClaimantUserDetails().getEmail()
                : caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (caseData.isRespondent1LiP() && StringUtils.isNotBlank(caseData.getRespondent1().getPartyEmail())) {
            return caseData.getRespondent1().getPartyEmail();
        } else {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
    }

    protected String getNotificationTemplate(CallbackParams params) {
        if (isLiP(params)) {
            return isBilingual(params)
                ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getNotifyLRCaseDismissed();
        }
    }

    private boolean isBilingual(CallbackParams params) {
        return (CaseEvent.NOTIFY_CLAIMANT_DISMISS_CASE.name().equals(params.getRequest().getEventId())
            && params.getCaseData().isClaimantBilingual())
            || (CaseEvent.NOTIFY_DEFENDANT_DISMISS_CASE.name().equals(params.getRequest().getEventId())
            && params.getCaseData().isRespondentResponseBilingual());
    }

    private boolean isLiP(CallbackParams params) {
        return (CaseEvent.NOTIFY_CLAIMANT_DISMISS_CASE.name().equals(params.getRequest().getEventId())
            && params.getCaseData().isApplicantLiP())
            || (CaseEvent.NOTIFY_DEFENDANT_DISMISS_CASE.name().equals(params.getRequest().getEventId())
            && params.getCaseData().isRespondent1LiP());
    }
}
