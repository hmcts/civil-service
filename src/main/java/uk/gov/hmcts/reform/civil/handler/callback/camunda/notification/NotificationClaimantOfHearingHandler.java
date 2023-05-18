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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIMANT_HEARING;

@Service
@RequiredArgsConstructor
public class NotificationClaimantOfHearingHandler extends CallbackHandler implements NotificationData {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIMANT_HEARING);
    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    private static final String REFERENCE_TEMPLATE_HEARING_LIP = "notification-of-hearing-lip-%s";
    public static final String TASK_ID_CLAIMANT = "NotifyClaimantHearing";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantHearing
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID_CLAIMANT;
    }

    private CallbackResponse notifyClaimantHearing(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        boolean isApplicantLip = isApplicantLip(caseData);
        sendEmail(caseData, getRecipient(caseData, isApplicantLip), getReferenceTemplate(caseData, isApplicantLip), isApplicantLip);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private void sendEmail(CaseData caseData, String recipient, String reference, boolean isApplicantLip) {
        notificationService.sendMail(recipient, getEmailTemplate(caseData, isApplicantLip), addProperties(caseData), reference);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(final CaseData caseData) {
        String reference = "";
        String legacyCaseRef = caseData.getLegacyCaseReference();
        String hearingDate = NotificationUtils.getFormattedHearingDate(caseData);
        String hearingTime = NotificationUtils.getFormattedHearingTime(caseData);
        Map<String, String> map = new HashMap<>(Map.of(CLAIM_REFERENCE_NUMBER, legacyCaseRef,
            HEARING_DATE, hearingDate, HEARING_TIME, hearingTime));
        if (!isApplicantLip(caseData)) {
            if (nonNull(caseData.getSolicitorReferences())
                && nonNull(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())) {
                reference = caseData.getSolicitorReferences().getApplicantSolicitor1Reference();
            }
            map.put(HEARING_FEE, caseData.getHearingFee() == null ? "£0.00" : String.valueOf(caseData.getHearingFee().formData()));
            map.put(HEARING_DUE_DATE, caseData.getHearingDueDate() == null ? "" : caseData.getHearingDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
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
        if (nonNull(caseData.getHearingFee()) && caseData.getHearingFee().getCalculatedAmountInPence().compareTo(BigDecimal.ZERO) > 0) {
            return notificationsProperties.getHearingListedFeeClaimantLrTemplate();
        } else {
            return notificationsProperties.getHearingListedNoFeeClaimantLrTemplate();
        }
    }

}
