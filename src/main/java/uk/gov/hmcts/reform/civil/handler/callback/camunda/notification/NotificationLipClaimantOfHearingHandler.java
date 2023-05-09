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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_LIP_CLAIMANT_HEARING;

@Service
@RequiredArgsConstructor
public class NotificationLipClaimantOfHearingHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_LIP_CLAIMANT_HEARING);
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-lip-of-hearing-%s";
    public static final String TASK_ID_CLAIMANT = "NotifyLipClaimantHearing";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyLipClaimantHearing
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_CLAIMANT;
    }

    private CallbackResponse notifyLipClaimantHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
        sendEmail(caseData, recipient);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private void sendEmail(CaseData caseData, String recipient) {
        String emailTemplate;
        if (caseData.getHearingFee() != null && caseData.getHearingFee().getCalculatedAmountInPence().compareTo(
            BigDecimal.ZERO) > 0) {
            emailTemplate = notificationsProperties.getHearingListedFeeClaimantLrTemplate();
        } else {
            emailTemplate = notificationsProperties.getHearingListedNoFeeClaimantLrTemplate();
        }
        notificationService.sendMail(recipient, emailTemplate, addProperties(caseData),
                                     String.format(REFERENCE_TEMPLATE_HEARING, caseData.getHearingReferenceNumber())
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        String hourMinute = caseData.getHearingTimeHourMinute();
        int hours = Integer.parseInt(hourMinute.substring(0, 2));
        int minutes = Integer.parseInt(hourMinute.substring(2, 4));
        LocalTime time = LocalTime.of(hours, minutes, 0);
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER,
            caseData.getLegacyCaseReference(),
            HEARING_DATE,
            caseData.getHearingDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            HEARING_TIME,
            time.format(DateTimeFormatter.ofPattern("hh:mma")).replace("AM", "am").replace("PM", "pm")
        ));
    }
}
