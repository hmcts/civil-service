package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final HearingNoticeCamundaService camundaService;
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
    private static final String EVENT_NOT_FOUND_MESSAGE = "Callback handler received illegal event: %s";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String TIME_FORMAT = "hh:mma";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantHearing
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (NOTIFY_DEFENDANT1_HEARING.equals(caseEvent)) {
            return TASK_ID_DEFENDANT1;
        } else if (NOTIFY_DEFENDANT2_HEARING.equals(caseEvent)) {
            return TASK_ID_DEFENDANT2;
        } else if (NOTIFY_DEFENDANT1_HEARING_HMC.equals(caseEvent)) {
            return TASK_ID_DEFENDANT1_HMC;
        } else if (NOTIFY_DEFENDANT2_HEARING_HMC.equals(caseEvent)) {
            return TASK_ID_DEFENDANT2_HMC;
        } else {
            throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, caseEvent));
        }
    }

    private CallbackResponse notifyDefendantHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        String recipient = caseData.getRespondentSolicitor1EmailAddress();

        if (isEvent(callbackParams, NOTIFY_DEFENDANT1_HEARING)) {
            if (isRespondent1Lip(caseData) && caseData.getRespondent1().getPartyEmail() != null) {
                recipient = caseData.getRespondent1().getPartyEmail();
                sendEmail(caseData, recipient, true);
            }
            if (!isRespondent1Lip(caseData)) {
                sendEmail(caseData, recipient, true);
            }
        } else if (isEvent(callbackParams, NOTIFY_DEFENDANT2_HEARING)) {
            if (nonNull(caseData.getRespondentSolicitor2EmailAddress())) {
                recipient = caseData.getRespondentSolicitor2EmailAddress();
            }
            sendEmail(caseData, recipient, false);
        } else if (isEvent(callbackParams, NOTIFY_DEFENDANT1_HEARING_HMC)) {
            sendEmailHmc(caseData, recipient, true);
        } else if (isEvent(callbackParams, NOTIFY_DEFENDANT2_HEARING_HMC)) {
            if (nonNull(caseData.getRespondentSolicitor2EmailAddress())) {
                recipient = caseData.getRespondentSolicitor2EmailAddress();
                sendEmailHmc(caseData, recipient, false);
            }
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

    private void sendEmailHmc(CaseData caseData, String recipient, boolean isFirst) {
        String defRefNumber = "";
        String hearingId = camundaService
            .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingId();
        if (isFirst) {
            if (nonNull(caseData.getSolicitorReferences())
                && nonNull(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())) {
                defRefNumber = caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
            }
        } else {
            defRefNumber = caseData.getRespondentSolicitor2Reference() == null ? "" :
                caseData.getRespondentSolicitor2Reference();
        }
        Map<String, String> properties = addPropertiesHmc(caseData);
        properties.put(DEFENDANT_REFERENCE_NUMBER, defRefNumber);
        String emailTemplate = notificationsProperties.getHearingListedNoFeeDefendantLrTemplateHMC();
        notificationService.sendMail(recipient, emailTemplate, properties,
                                     String.format(REFERENCE_TEMPLATE_HEARING, hearingId)
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

    private boolean isRespondent1Lip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    public Map<String, String> addPropertiesHmc(final CaseData caseData) {
        LocalDateTime hearingStartDateTime = camundaService
            .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingStartDateTime();
        LocalDate hearingDate = hearingStartDateTime.toLocalDate();
        LocalTime hearingTime = hearingStartDateTime.toLocalTime();
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER,
            caseData.getLegacyCaseReference(),
            HEARING_DATE,
            hearingDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
            HEARING_TIME,
            hearingTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT)).replace("AM", "am").replace("PM", "pm")
        ));
    }
}
