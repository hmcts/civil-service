package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_DEFENDANT_RESPONSE_SUBMISSION;

@Service
@RequiredArgsConstructor
public class DefendantLipResponseRespondentNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public static final String TASK_ID = "DefendantLipResponseNotifyDefendant";
    private static final String REFERENCE_TEMPLATE =
        "defendant-lip-response-respondent-notification-%s";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::sendEmailToDefendant
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(NOTIFY_LIP_DEFENDANT_RESPONSE_SUBMISSION);
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    private CallbackResponse sendEmailToDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (StringUtils.isNotEmpty(caseData.getRespondent1().getPartyEmail())) {
            notificationService.sendMail(
                caseData.getRespondent1().getPartyEmail(),
                addTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String addTemplate(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            return notificationsProperties.getRespondentLipResponseSubmissionBilingualTemplate();
        } else {
            return notificationsProperties.getRespondentLipResponseSubmissionTemplate();
        }
    }
}
