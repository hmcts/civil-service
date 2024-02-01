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
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED;

@Service
@RequiredArgsConstructor
public class NotificationForClaimantRepresented extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_DEFENDANT =
        "notify-lip-after-noc-approval-%s";
    public static final String TASK_ID_APPLICANT = "NotifyClaimantLipAfterNocApproval";
    public static final String TASK_ID_RESPONDENT = "NotifyDefendantLipClaimantRepresented";
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyLipAfterNocApproval
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondentNotification(callbackParams) ? TASK_ID_RESPONDENT : TASK_ID_APPLICANT;
    }

    private CallbackResponse notifyLipAfterNocApproval(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipientEmail = isRespondentNotification(callbackParams) ? getRecipientEmailForRespondent(caseData) :
            caseData.getApplicant1Email();
        String templateId = getTemplateID(
            isRespondentNotification(callbackParams), caseData
        );
        if (isNotEmpty(recipientEmail) && templateId != null) {
            notificationService.sendMail(
                recipientEmail,
                templateId,
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE_DEFENDANT, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(
            NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED,
            NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    private boolean isRespondentNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED.name());
    }

    private String getRecipientEmailForRespondent(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1())
            .map(Party::getPartyEmail)
            .orElse("");
    }

    private String getTemplateID(boolean isDefendantEvent, CaseData caseData) {
        if (isDefendantEvent) {
            return notificationsProperties.getNotifyRespondentLipForClaimantRepresentedTemplate();
        }
        if (caseData.isBilingual()) {
            return notificationsProperties.getNotifyClaimantLipForNoLongerAccessWelshTemplate();
        }
        return notificationsProperties.getNotifyClaimantLipForNoLongerAccessTemplate();
    }
}
