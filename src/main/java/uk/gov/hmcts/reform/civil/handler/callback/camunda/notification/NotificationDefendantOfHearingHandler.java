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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@Service
@RequiredArgsConstructor
public class NotificationDefendantOfHearingHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_DEFENDANT_HEARING);
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    public static final String TASK_ID_DEFENDANT = "NotifyDefendantHearing";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantHearing
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_DEFENDANT;
    }

    private CallbackResponse notifyDefendantHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = caseData.getRespondentSolicitor1EmailAddress();
        if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)
            || getMultiPartyScenario(caseData).equals(ONE_V_ONE)) {
            sendEmail(caseData, recipient, true);
        } else {
            String recipient2 = caseData.getRespondentSolicitor2EmailAddress();
            sendEmail(caseData, recipient, true);
            sendEmail(caseData, recipient2, false);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private void sendEmail(CaseData caseData, String recipient, Boolean isFirst) {
        String defRefNumber;
        if (isFirst) {
            if (caseData.getSolicitorReferences() == null
                || caseData.getSolicitorReferences().getRespondentSolicitor1Reference() == null) {
                defRefNumber = "";
            } else {
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
                                     String.format(REFERENCE_TEMPLATE_HEARING, caseData.getHearingReference())
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
            HEARING_FEE,
            caseData.getHearingFee() == null ? "£0.00" : String.valueOf(caseData.getHearingFee().formData()),
            HEARING_DATE,
            caseData.getHearingDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            HEARING_TIME,
            time.toString(),
            HEARING_DUE_DATE,
            caseData.getHearingDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        ));
    }
}
