package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@Service
@RequiredArgsConstructor
public class NotificationForDisAgreedRePaymentPlanHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_CLAIMANT_FOR_RESPONDENT1_DISAGREED_REPAYMENT, CaseEvent.NOTIFY_LIP_DEFENDANT_DISAGREED_REPAYMENT);
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-%s";
    public static final String TASK_ID_CLAIMANT = "ClaimantDisAgreedRepaymentPlanNotifyApplicant";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantLIP
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_CLAIMANT;
    }

    private CallbackResponse notifyClaimantLIP(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
        sendEmail(caseData, recipient);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private void sendEmail(CaseData caseData, String recipient) {

        String  emailTemplate = notificationsProperties.getNotifyClaimantLrTemplate();
        String  emailLipTemplate = notificationsProperties.getNotifyDefendantLipTemplate();
        notificationService.sendMail(recipient, emailTemplate, addProperties(caseData),
                                     String.format(REFERENCE_TEMPLATE_HEARING, caseData.getHearingReferenceNumber()));
        notificationService.sendMail(recipient, emailLipTemplate, addProperties(caseData),
                                     String.format(REFERENCE_TEMPLATE_HEARING, caseData.getHearingReferenceNumber()));
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        String reference;
        if (caseData.getSolicitorReferences() == null
            || caseData.getSolicitorReferences().getApplicantSolicitor1Reference() == null) {
            reference = "";
        } else {
            reference = caseData.getSolicitorReferences().getApplicantSolicitor1Reference();
        }
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER,
            caseData.getLegacyCaseReference(),
            CLAIMANT_REFERENCE_NUMBER, reference
        ));
    }
}
