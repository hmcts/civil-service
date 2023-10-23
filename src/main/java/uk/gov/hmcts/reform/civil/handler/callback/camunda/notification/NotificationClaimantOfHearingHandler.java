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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_HEARING;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_HEARING_HMC;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateHearingDueDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isEvent;

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
    private static final String REFERENCE_TEMPLATE_HEARING_LIP = "notification-of-hearing-lip-%s";
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
        boolean isApplicantLip = isApplicantLip(caseData);
        String recipient = caseData.getApplicantSolicitor1UserDetails().getEmail();

        if (isEvent(callbackParams, NOTIFY_CLAIMANT_HEARING_HMC)) {
            sendEmailHMC(caseData, recipient);
        } else if (isEvent(callbackParams, NOTIFY_CLAIMANT_HEARING)) {
            sendEmail(caseData, getRecipient(caseData, isApplicantLip), getReferenceTemplate(caseData, isApplicantLip), isApplicantLip);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private void sendEmail(CaseData caseData, String recipient, String reference, boolean isApplicantLip) {
        notificationService.sendMail(recipient, getEmailTemplate(caseData, isApplicantLip), addProperties(caseData), reference);
    }

    private void sendEmailHMC(CaseData caseData, String recipient) {
        String emailTemplate;
        if (caseData.getHearingFeePaymentDetails() != null
            && SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus())) {
            emailTemplate = notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC();
        } else {
            emailTemplate = notificationsProperties.getHearingListedFeeClaimantLrTemplateHMC();
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
        String reference = "";
        String legacyCaseRef = caseData.getLegacyCaseReference();
        String hearingDate = NotificationUtils.getFormattedHearingDate(caseData.getHearingDate());
        String hearingTime = NotificationUtils.getFormattedHearingTime(caseData.getHearingTimeHourMinute());
        Map<String, String> map = new HashMap<>(Map.of(CLAIM_REFERENCE_NUMBER, legacyCaseRef,
            HEARING_DATE, hearingDate, HEARING_TIME, hearingTime));
        if (!isApplicantLip(caseData)) {
            if (nonNull(caseData.getSolicitorReferences())
                && nonNull(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())) {
                reference = caseData.getSolicitorReferences().getApplicantSolicitor1Reference();
            }
            if (caseData.getHearingFeePaymentDetails() == null
                || !SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus())) {
                map.put(HEARING_FEE, caseData.getHearingFee() == null
                    ? "£0.00" : String.valueOf(caseData.getHearingFee().formData()));
                map.put(HEARING_DUE_DATE, caseData.getHearingDueDate() == null
                    ? "" : NotificationUtils.getFormattedHearingDate(caseData.getHearingDueDate()));
            }
            map.put(CLAIMANT_REFERENCE_NUMBER, reference);
        }
        return map;
    }

    private boolean isApplicantLip(CaseData caseData) {
        return (YesOrNo.NO.equals(caseData.getApplicant1Represented()));
    }

    private String getRecipient(CaseData caseData, boolean isApplicantLip) {
        return isApplicantLip ? caseData.getApplicant1().getPartyEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    private String getReferenceTemplate(CaseData caseData, boolean isApplicantLip) {
        return isApplicantLip ? String.format(REFERENCE_TEMPLATE_HEARING_LIP, caseData.getHearingReferenceNumber())
            : String.format(REFERENCE_TEMPLATE_HEARING, caseData.getHearingReferenceNumber());
    }

    private String getEmailTemplate(CaseData caseData, boolean isApplicantLip) {
        if (isApplicantLip) {
            return notificationsProperties.getHearingNotificationLipDefendantTemplate();
        }
        if (caseData.getHearingFeePaymentDetails() != null
            && SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus())) {
            return notificationsProperties.getHearingListedNoFeeClaimantLrTemplate();
        } else {
            return notificationsProperties.getHearingListedFeeClaimantLrTemplate();
        }
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
