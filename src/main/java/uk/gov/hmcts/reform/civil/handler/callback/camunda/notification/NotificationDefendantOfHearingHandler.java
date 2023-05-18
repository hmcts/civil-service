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
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT1_HEARING;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DEFENDANT2_HEARING;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isDefendant1;

@Service
@RequiredArgsConstructor
public class NotificationDefendantOfHearingHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DEFENDANT1_HEARING, NOTIFY_DEFENDANT2_HEARING);
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    private static final String REFERENCE_TEMPLATE_HEARING_LIP = "notification-of-hearing-lip-%s";
    public static final String TASK_ID_DEFENDANT1 = "NotifyDefendant1Hearing";
    public static final String TASK_ID_DEFENDANT2 = "NotifyDefendant2Hearing";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyDefendantHearing
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return isDefendant1(callbackParams, NOTIFY_DEFENDANT1_HEARING) ? TASK_ID_DEFENDANT1
            : TASK_ID_DEFENDANT2;
    }

    private CallbackResponse notifyDefendantHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isRespondentLip = isRespondentLip(caseData);
        boolean isDefendant1 = isDefendant1(callbackParams, NOTIFY_DEFENDANT1_HEARING);
        sendEmail(caseData, getRespondentRecipient(caseData, isDefendant1, isRespondentLip), isDefendant1, isRespondentLip);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private void sendEmail(CaseData caseData, String recipient, boolean isDefendant1, boolean isRespondentLip) {
        Map<String, String> properties = addProperties(caseData);
        if (!isRespondentLip) {
            properties.put(DEFENDANT_REFERENCE_NUMBER, getDefRefNumber(caseData, isDefendant1));
        }
        notificationService.sendMail(recipient, getEmailTemplate(isRespondentLip), properties, getReferenceTemplate(caseData, isRespondentLip));
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        String legacyCaseRef = caseData.getLegacyCaseReference();
        String hearingDate = NotificationUtils.getFormattedHearingDate(caseData);
        String hearingTime = NotificationUtils.getFormattedHearingTime(caseData);
        return new HashMap<>(Map.of(CLAIM_REFERENCE_NUMBER, legacyCaseRef, HEARING_DATE, hearingDate, HEARING_TIME, hearingTime));
    }

    private boolean isRespondentLip(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    }

    private String getRespondentRecipient(CaseData caseData, boolean isDefendant1, boolean isRespondentLip) {
        if (isDefendant1) {
            return isRespondentLip ? caseData.getRespondent1().getPartyEmail()
                : caseData.getRespondentSolicitor1EmailAddress();
        } else {
            if (!isRespondentLip && nonNull(caseData.getRespondentSolicitor2EmailAddress())) {
                return caseData.getRespondentSolicitor2EmailAddress();
            } else if (!isRespondentLip) {
                return caseData.getRespondentSolicitor1EmailAddress();
            } else if (isRespondentLip && nonNull(caseData.getRespondent2().getPartyEmail())) {
                return caseData.getRespondent2().getPartyEmail();
            }
            return null;
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

    private String getEmailTemplate(boolean isRespondentLip) {
        return isRespondentLip ? notificationsProperties.getHearingNotificationLipDefendantTemplate()
            :  notificationsProperties.getHearingListedNoFeeDefendantLrTemplate();
    }

    private String getReferenceTemplate(CaseData caseData, boolean isRespondentLip) {
        return isRespondentLip ? String.format(REFERENCE_TEMPLATE_HEARING_LIP, caseData.getHearingReferenceNumber())
            : String.format(REFERENCE_TEMPLATE_HEARING, caseData.getHearingReferenceNumber());
    }
}
