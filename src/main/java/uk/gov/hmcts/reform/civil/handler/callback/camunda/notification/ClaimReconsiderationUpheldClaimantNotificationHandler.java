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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;

@Service
@RequiredArgsConstructor
public class ClaimReconsiderationUpheldClaimantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIM_RECONSIDERATION_UPHELD_CLAIMANT);
    public static final String TASK_ID = "NotifyClaimReconsiderationUpheld";
    private static final String REFERENCE_TEMPLATE =
        "hearing-fee-unpaid-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimReconsiderationUpheld
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

    private CallbackResponse notifyClaimReconsiderationUpheld(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        notificationService.sendMail(
            getApplicantEmail(caseData),
            getTemplate(),
            addProperties(caseData),
            getReferenceTemplate(caseData)
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData),
                PARTY_NAME, caseData.getApplicant1().getPartyName()
            );
    }

    private String getTemplate() {
        return notificationsProperties.getNotifyUpdateTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    private String getApplicantEmail(CaseData caseData) {
        return caseData.getApplicantSolicitor1UserDetails().getEmail() != null
            ? caseData.getApplicantSolicitor1UserDetails().getEmail() : caseData.getClaimantUserDetails().getEmail();
    }
}
