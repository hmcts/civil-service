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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED;

@Service
@RequiredArgsConstructor
public class NotificationForClaimantRepresented extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final String REFERENCE_TEMPLATE_DEFENDANT = "notify-defendant-lip-claimant-represented-notification-%s";
    public static final String TASK_ID = "NotifyDefendantLipClaimantRepresented";
    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantLIP
    );

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyDefendantLIP(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (StringUtils.isNotEmpty(caseData.getRespondent1().getPartyEmail())) {
            notificationService.sendMail(
                caseData.getRespondent1().getPartyEmail(),
                notificationsProperties.getNotifyRespondentLipForClaimantRepresentedTemplate(),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE_DEFENDANT, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(
            NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED
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
}
