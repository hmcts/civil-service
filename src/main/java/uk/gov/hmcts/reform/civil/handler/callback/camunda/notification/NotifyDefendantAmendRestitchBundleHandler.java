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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@Service
@RequiredArgsConstructor
public class NotifyDefendantAmendRestitchBundleHandler extends CallbackHandler implements NotificationData {

    private static final String TASK_ID = "NotifyDefendantAmendRestitchBundle";
    private static final String REFERENCE_TEMPLATE = "amend-restitch-bundle-defendant-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DEFENDANT_AMEND_RESTITCH_BUNDLE);
    private static final String DATE_FORMAT = "dd-MM-yyyy";

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
            getRecipient(caseData),
            getNotificationTemplate(caseData),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getNotificationTemplate(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            return caseData.isRespondentResponseBilingual()
                ? notificationsProperties.getNotifyLipUpdateTemplateBilingual()
                : notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getNotifyLRBundleRestitched();
        }
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            CLAIMANT_V_DEFENDANT, PartyUtils.getAllPartyNames(caseData),
            BUNDLE_RESTITCH_DATE, LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK)),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            LEGACY_CLAIM_REFERENCE, caseData.getLegacyCaseReference()
        );
    }

    private String getRecipient(CaseData caseData) {
        if (caseData.isRespondent1LiP() && nonNull(caseData.getRespondent1().getPartyEmail())) {
            return caseData.getRespondent1().getPartyEmail();
        } else {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
    }
}
