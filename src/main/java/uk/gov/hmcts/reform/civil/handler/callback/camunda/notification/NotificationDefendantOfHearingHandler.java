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
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.time.LocalDateTime;
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
    private static final String REFERENCE_TEMPLATE_HEARING_LIP = "notification-of-hearing-lip-%s";
    public static final String TASK_ID_DEFENDANT1 = "NotifyDefendant1Hearing";
    public static final String TASK_ID_DEFENDANT2 = "NotifyDefendant2Hearing";
    public static final String TASK_ID_DEFENDANT1_HMC = "NotifyDefendantSolicitor1Hearing";
    public static final String TASK_ID_DEFENDANT2_HMC = "NotifyDefendantSolicitor2Hearing";
    private static final String EVENT_NOT_FOUND_MESSAGE = "Callback handler received illegal event: %s";

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
        boolean isRespondent1Lip = isRespondent1Lip(caseData);
        boolean isDefendant1 = isEvent(callbackParams, NOTIFY_DEFENDANT1_HEARING) || isEvent(callbackParams, NOTIFY_DEFENDANT1_HEARING_HMC);
        boolean isHmc = isEvent(callbackParams, NOTIFY_DEFENDANT1_HEARING_HMC) || isEvent(callbackParams, NOTIFY_DEFENDANT2_HEARING_HMC);
        sendEmail(caseData, getRespondentRecipient(caseData, isDefendant1, isRespondent1Lip), isDefendant1, isRespondent1Lip, isHmc);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private void sendEmail(CaseData caseData, String recipient, boolean isDefendant1, boolean isRespondent1Lip, boolean isHmc) {
        Map<String, String> properties;
        if (isHmc) {
            properties = addPropertiesHmc(caseData);
        } else {
            properties = addProperties(caseData);
        }
        if (!isRespondent1Lip || isHmc) {
            properties.put(DEFENDANT_REFERENCE_NUMBER, getDefRefNumber(caseData, isDefendant1));
        }
        notificationService.sendMail(recipient, getEmailTemplate(isRespondent1Lip, isHmc), properties, getReferenceTemplate(caseData, isRespondent1Lip, isHmc));
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        String legacyCaseRef = caseData.getLegacyCaseReference();
        String hearingDate = NotificationUtils.getFormattedHearingDate(caseData.getHearingDate());
        String hearingTime = NotificationUtils.getFormattedHearingTime(caseData.getHearingTimeHourMinute());
        return new HashMap<>(Map.of(CLAIM_REFERENCE_NUMBER, legacyCaseRef, HEARING_DATE, hearingDate, HEARING_TIME, hearingTime));
    }

    public Map<String, String> addPropertiesHmc(final CaseData caseData) {
        LocalDateTime hearingStartDateTime = camundaService
            .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingStartDateTime();
        return new HashMap<>(Map.of(
            CLAIM_REFERENCE_NUMBER,
            caseData.getLegacyCaseReference(),
            HEARING_DATE,
            NotificationUtils.getFormattedHearingDate(hearingStartDateTime.toLocalDate()),
            HEARING_TIME,
            NotificationUtils.getFormattedHearingTime(hearingStartDateTime.toLocalTime().toString())
        ));
    }

    private boolean isRespondent1Lip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    private String getRespondentRecipient(CaseData caseData, boolean isDefendant1, boolean isRespondent1Lip) {
        if (isDefendant1) {
            return isRespondent1Lip ? caseData.getRespondent1().getPartyEmail()
                : caseData.getRespondentSolicitor1EmailAddress();
        } else {
            if (!isRespondent1Lip) {
                if (nonNull(caseData.getRespondentSolicitor2EmailAddress())) {
                    return caseData.getRespondentSolicitor2EmailAddress();
                } else {
                    return caseData.getRespondentSolicitor1EmailAddress();
                }
            } else {
                if (nonNull(caseData.getRespondent2().getPartyEmail())) {
                    return caseData.getRespondent2().getPartyEmail();
                } else {
                    return caseData.getRespondent1().getPartyEmail();
                }
            }
        }
    }

    private String getDefRefNumber(CaseData caseData, boolean isDefendant1) {
        if (isDefendant1) {
            if (nonNull(caseData.getSolicitorReferences())
                && nonNull(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())) {
                return caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
            }
        } else {
            return caseData.getRespondentSolicitor2Reference() == null ? "" :
                caseData.getRespondentSolicitor2Reference();
        }
        return "";
    }

    private String getEmailTemplate(boolean isRespondent1Lip, boolean isHmc) {
        if (isHmc) {
            return notificationsProperties.getHearingListedNoFeeDefendantLrTemplateHMC();
        } else {
            return isRespondent1Lip ? notificationsProperties.getHearingNotificationLipDefendantTemplate()
                : notificationsProperties.getHearingListedNoFeeDefendantLrTemplate();
        }
    }

    private String getReferenceTemplate(CaseData caseData, boolean isRespondent1Lip, boolean isHmc) {
        if (isHmc) {
            return String.format(REFERENCE_TEMPLATE_HEARING, camundaService
            .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingId());
        } else {
            return isRespondent1Lip ? String.format(REFERENCE_TEMPLATE_HEARING_LIP, caseData.getHearingReferenceNumber())
                : String.format(REFERENCE_TEMPLATE_HEARING, caseData.getHearingReferenceNumber());
        }
    }

}
