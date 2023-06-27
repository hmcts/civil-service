package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_HEARING;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_HEARING_HMC;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateHearingDueDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationClaimantOfHearingHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final HearingFeesService hearingFeesService;
    private final NotificationsProperties notificationsProperties;
    private final HearingNoticeCamundaService camundaService;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_HMC);
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    public static final String TASK_ID_CLAIMANT = "NotifyClaimantHearing";
    public static final String TASK_ID_CLAIMANT_HMC = "NotifyClaimantSolicitorHearing";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Callback handler received illegal event: %s";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String TIME_FORMAT = "hh:mma";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantHearing
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (NOTIFY_CLAIMANT_HEARING.equals(caseEvent)) {
            return TASK_ID_CLAIMANT;
        } else if (NOTIFY_CLAIMANT_HEARING_HMC.equals(caseEvent)) {
            return TASK_ID_CLAIMANT_HMC;
        } else {
            throw new CallbackException(String.format(EVENT_NOT_FOUND_MESSAGE, caseEvent));
        }
    }

    private CallbackResponse notifyClaimantHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (NOTIFY_CLAIMANT_HEARING.equals(caseEvent)) {
            sendEmail(caseData, recipient);
        }
        if (NOTIFY_CLAIMANT_HEARING_HMC.equals(caseEvent)) {
            sendEmailHMC(caseData, recipient);
        }
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

    private void sendEmailHMC(CaseData caseData, String recipient) {
        String emailTemplate;
        Fee fee = calculateAndApplyFee(hearingFeesService, caseData, caseData.getAllocatedTrack());
        if (fee != null && fee.getCalculatedAmountInPence().compareTo(
            BigDecimal.ZERO) > 0) {
            emailTemplate = notificationsProperties.getHearingListedFeeClaimantLrTemplateHMC();
        } else {
            emailTemplate = notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC();
        }
        String hearingId = camundaService
            .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingId();
        notificationService.sendMail(recipient, emailTemplate, addPropertiesHMC(caseData),
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
        String reference;
        int hours = Integer.parseInt(hourMinute.substring(0, 2));
        int minutes = Integer.parseInt(hourMinute.substring(2, 4));
        LocalTime time = LocalTime.of(hours, minutes, 0);
        if (caseData.getSolicitorReferences() == null
            || caseData.getSolicitorReferences().getApplicantSolicitor1Reference() == null) {
            reference = "";
        } else {
            reference = caseData.getSolicitorReferences().getApplicantSolicitor1Reference();
        }
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER,
            caseData.getLegacyCaseReference(),
            HEARING_FEE,
            caseData.getHearingFee() == null ? "£0.00" : String.valueOf(caseData.getHearingFee().formData()),
            HEARING_DATE,
            caseData.getHearingDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            HEARING_TIME,
            time.format(DateTimeFormatter.ofPattern("hh:mma")).replace("AM", "am").replace("PM", "pm"),
            HEARING_DUE_DATE,
            caseData.getHearingDueDate() == null ? "" :
                caseData.getHearingDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            CLAIMANT_REFERENCE_NUMBER, reference

        ));
    }

    public Map<String, String> addPropertiesHMC(final CaseData caseData) {
        Fee fee = calculateAndApplyFee(hearingFeesService, caseData, caseData.getAllocatedTrack());
        LocalDateTime hearingStartDateTime = camundaService
            .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingStartDateTime();

        LocalDate hearingDate = hearingStartDateTime.toLocalDate();
        LocalTime hearingTime = hearingStartDateTime.toLocalTime();

        return Map.of(
            CLAIM_REFERENCE_NUMBER,
            caseData.getLegacyCaseReference(),
            HEARING_FEE,
            fee == null ? "£0.00" : String.valueOf(fee.formData()),
            HEARING_DATE,
            hearingDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)),
            HEARING_TIME,
            hearingTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT)).replace("AM", "am").replace(
                "PM",
                "pm"
            ),
            HEARING_DUE_DATE,
            calculateHearingDueDate(LocalDate.now(), hearingDate).format(DateTimeFormatter.ofPattern(DATE_FORMAT))
        );
    }
}
