package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class ClaimantResponseNotAgreedRepaymentDefendantLipNotificationHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_LIP_DEFENDANT_REJECT_REPAYMENT);
    private static final String REFERENCE_TEMPLATE_LIP = "claimant-reject-repayment-respondent-notification-%s";
    public static final String TASK_ID_LIP = "ClaimantDisAgreedRepaymentPlanNotifyLip";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantLip
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_LIP;
    }

    private CallbackResponse notifyDefendantLip(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isRespondentNotRepresented(caseData) && caseData.getRespondent1().getPartyEmail() != null) {
            notificationService.sendMail(
                addEmail(caseData),
                addTemplate(caseData),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE_LIP, caseData.getLegacyCaseReference()));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();

    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {

        if (isRespondentNotRepresented(caseData)) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        }
        return null;
    }

    private String addEmail(CaseData caseData) {
        if (isRespondentNotRepresented(caseData)) {
            return caseData.getRespondent1().getPartyEmail();
        }
        return null;
    }

    private String addTemplate(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            return notificationsProperties.getNotifyDefendantLipWelshTemplate();
        } else {
            return notificationsProperties.getNotifyDefendantLipTemplate();
        }
    }

    public boolean isRespondentNotRepresented(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getSpecRespondent1Represented());
    }
}
