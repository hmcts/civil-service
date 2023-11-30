package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_RESPONDENT_FOR_SIGN_SETTLEMENT_AGREEMENT;

@Service
@RequiredArgsConstructor
public class DefendantSignSettlementAgreementNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS =
        List.of(
            CaseEvent.NOTIFY_LIP_APPLICANT_FOR_SIGN_SETTLEMENT_AGREEMENT,
            CaseEvent.NOTIFY_LIP_RESPONDENT_FOR_SIGN_SETTLEMENT_AGREEMENT
        );
    public static final String TASK_ID_APPLICANT = "NotifyApplicantForSignSettlementAgreement";
    public static final String TASK_ID_RESPONDENT = "NotifyRespondentForSignSettlementAgreement";
    private static final String REFERENCE_TEMPLATE = "notify-signed-settlement-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final Map<String, Callback> callBackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyForSignedSettlement
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callBackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondentNotification(callbackParams) ? TASK_ID_RESPONDENT : TASK_ID_APPLICANT;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            FRONTEND_URL, pipInPostConfiguration.getCuiFrontEndUrl()
        );
    }

    private CallbackResponse notifyForSignedSettlement(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Map<String, String> templateProperties = addProperties(caseData);
        String recipientEmail = isRespondentNotification(callbackParams) ? getRecipientEmailForRespondent(caseData) :
            caseData.getApplicant1Email();
        String templateId = isRespondentNotification(callbackParams) ? getTemplateID(
            caseData,
            notificationsProperties.getNotifyRespondentForSignedSettlementAgreement(),
            notificationsProperties.getNotifyRespondentForNotAgreedSignSettlement()
        ) : getTemplateID(
            caseData,
            notificationsProperties.getNotifyApplicantForSignedSettlementAgreement(),
            notificationsProperties.getNotifyApplicantForNotAgreedSignSettlement()
        );
        if (isNotEmpty(recipientEmail)) {
            notificationService.sendMail(
                recipientEmail,
                templateId,
                templateProperties,
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private boolean isRespondentNotification(CallbackParams callbackParams) {
        return callbackParams.getRequest().getEventId()
            .equals(NOTIFY_LIP_RESPONDENT_FOR_SIGN_SETTLEMENT_AGREEMENT.name());
    }

    private String getTemplateID(CaseData caseData, String agreedTemplate, String notAgreedTemplate) {
        Optional<CaseDataLiP> optionalCaseDataLiP = Optional.ofNullable(caseData.getCaseDataLiP());
        boolean isAgreed = optionalCaseDataLiP.map(CaseDataLiP::isDefendantSignedSettlementAgreement).orElse(false);
        boolean isNotAgreed = optionalCaseDataLiP.map(CaseDataLiP::isDefendantSignedSettlementNotAgreed).orElse(false);
        return isAgreed ? agreedTemplate : (isNotAgreed ? notAgreedTemplate : null);
    }

    private String getRecipientEmailForRespondent(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1())
            .map(Party::getPartyEmail)
            .orElse("");
    }
}
