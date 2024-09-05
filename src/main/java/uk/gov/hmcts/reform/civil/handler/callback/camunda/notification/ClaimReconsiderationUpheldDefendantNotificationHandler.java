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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;

@Service
@RequiredArgsConstructor
public class ClaimReconsiderationUpheldDefendantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIM_RECONSIDERATION_UPHELD_DEFENDANT);
    public static final String TASK_ID = "NotifyClaimRreconsiderationUpheld";
    private static final String REFERENCE_TEMPLATE =
        "reconsideration-upheld-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimReconsiderationUpheldToDefendant
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyClaimReconsiderationUpheldToDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getRespondent1() != null && !caseData.getRespondent1().getPartyName().isEmpty()) {
            notificationService.sendMail(
                caseData.isRespondent1LiP() ? caseData.getRespondent1().getPartyEmail() : caseData.getRespondentSolicitor1EmailAddress(),
                getTemplate(),
                addProperties(caseData),
                getReferenceTemplate(caseData)
            );
        }
        if (caseData.getRespondent2() != null && !caseData.getRespondent2().getPartyName().isEmpty()) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor2EmailAddress() != null
                    ? caseData.getRespondentSolicitor2EmailAddress() :
                    caseData.getRespondentSolicitor1EmailAddress(),
                getTemplate(),
                addPropertiesDef2(caseData),
                getReferenceTemplate(caseData)
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData),
            PARTY_NAME, caseData.getRespondent1().getPartyName()
        );
    }

    private String getTemplate() {
        return notificationsProperties.getNotifyClaimReconsiderationLRTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getCcdCaseReference().toString());
    }

    public Map<String, String> addPropertiesDef2(final CaseData caseData) {
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData),
            PARTY_NAME, caseData.getRespondent2().getPartyName()
        ));
    }

}
