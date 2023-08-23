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
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_HEARING_FEE_UNPAID;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.is1v1Or2v1Case;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isRespondent1;

@Service
@RequiredArgsConstructor
public class HearingFeeUnpaidRespondentNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_HEARING_FEE_UNPAID,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_HEARING_FEE_UNPAID
    );
    public static final String TASK_ID_RESPONDENT1 = "HearingFeeUnpaidNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT2 = "HearingFeeUnpaidNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE =
        "hearing-fee-unpaid-respondent-notification-%s";
    private static final String REFERENCE_TEMPLATE_DEFENDANT_LIP =
        "hearing-fee-unpaid-defendantLip-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyRespondentSolicitorForHearingFeeUnpaid
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_HEARING_FEE_UNPAID) ? TASK_ID_RESPONDENT1
            : TASK_ID_RESPONDENT2;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyRespondentSolicitorForHearingFeeUnpaid(
        CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = !is1v1Or2v1Case(caseData)
            && !isRespondent1(callbackParams, NOTIFY_RESPONDENT_SOLICITOR1_FOR_HEARING_FEE_UNPAID) ? caseData
                .getRespondentSolicitor2EmailAddress() : getRecipientRespondent1(caseData);

        if (nonNull(recipient)) {
            notificationService.sendMail(
                recipient,
                getTemplate(caseData),
                isRespondent1Lip(caseData) ? addPropertiesRespondentLip(caseData)
                    : addProperties(caseData),
                isRespondent1Lip(caseData)
                    ? String.format(REFERENCE_TEMPLATE_DEFENDANT_LIP, caseData.getLegacyCaseReference())
                    : String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE)
        );
    }

    public Map<String, String> addPropertiesRespondentLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData),
            PARTY_NAME, caseData.getRespondent1().getPartyName()
        );
    }

    private String getRecipientRespondent1(CaseData caseData) {
        return isRespondent1Lip(caseData) ? caseData.getRespondent1().getPartyEmail()
            : caseData.getRespondentSolicitor1EmailAddress();
    }

    private String getTemplate(CaseData caseData) {
        if (isRespondent1Lip(caseData)) {
            return notificationsProperties.getNotifyLipUpdateTemplate();
        }
        return notificationsProperties.getRespondentHearingFeeUnpaid();
    }

    private boolean isRespondent1Lip(CaseData caseData) {
        return (YesOrNo.NO.equals(caseData.getRespondent1Represented()));
    }
}
