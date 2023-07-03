package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT1_HEARING;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT1_HEARING_HMC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_HEARING;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_HEARING_HMC;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isEvent;

@Service
@RequiredArgsConstructor
public class NotificationDefendantOfHearingHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_DEFENDANT1_HEARING,
        NOTIFY_DEFENDANT2_HEARING,
        NOTIFY_DEFENDANT1_HEARING_HMC,
        NOTIFY_DEFENDANT2_HEARING_HMC
    );
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    public static final String TASK_ID_DEFENDANT1 = "NotifyDefendant1Hearing";
    public static final String TASK_ID_DEFENDANT2 = "NotifyDefendant2Hearing";
    public static final String TASK_ID_DEFENDANT1_HMC = "NotifyDefendantSolicitor1Hearing";
    public static final String TASK_ID_DEFENDANT2_HMC = "NotifyDefendantSolicitor2Hearing";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantHearing
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isEvent(callbackParams, NOTIFY_DEFENDANT1_HEARING) ? TASK_ID_DEFENDANT1
            : TASK_ID_DEFENDANT2;
    }

    private CallbackResponse notifyDefendantHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        //ToDo: Replace with AHN logic
        if (isEvent(callbackParams, NOTIFY_DEFENDANT1_HEARING_HMC) || isEvent(callbackParams, NOTIFY_DEFENDANT2_HEARING_HMC)) {
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        String recipient = caseData.getRespondentSolicitor1EmailAddress();

        if (isEvent(callbackParams, NOTIFY_DEFENDANT1_HEARING)) {
            sendEmail(caseData, recipient, true);
        } else {
            if (nonNull(caseData.getRespondentSolicitor2EmailAddress())) {
                recipient = caseData.getRespondentSolicitor2EmailAddress();
            }
            sendEmail(caseData, recipient, false);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private void sendEmail(CaseData caseData, String recipient, boolean isFirst) {
        String defRefNumber = "";
        if (isFirst) {
            if (nonNull(caseData.getSolicitorReferences())
                && nonNull(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())) {
                defRefNumber = caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
            }
        } else {
            defRefNumber = caseData.getRespondentSolicitor2Reference() == null ? "" :
                caseData.getRespondentSolicitor2Reference();
        }
        Map<String, String> properties = addProperties(caseData);
        properties.put(DEFENDANT_REFERENCE_NUMBER, defRefNumber);
        String emailTemplate = notificationsProperties.getHearingListedNoFeeDefendantLrTemplate();
        notificationService.sendMail(recipient, emailTemplate, properties,
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
