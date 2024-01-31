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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class TrialReadyApplicantNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_SOLICITOR1_FOR_TRIAL_READY);

    public static final String TASK_ID = "TrialReadyNotifyApplicantSolicitor1";
    private static final String REFERENCE_TEMPLATE = "trial-ready-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorForTrialReady
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifyApplicantSolicitorForTrialReady(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (isLipApplicant(callbackParams)) {
            if (caseData.getApplicant1Email() != null) {
                notificationService.sendMail(
                    caseData.getApplicant1Email(),
                    notificationsProperties.getNotifyLipUpdateTemplate(),
                    addPropertiesLip(caseData),
                    String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
                );
            }
            return AboutToStartOrSubmitCallbackResponse.builder().build();
        }

        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getSolicitorTrialReady(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        String claimRefNumber;
        if (caseData.getSolicitorReferences() == null
            || caseData.getSolicitorReferences().getApplicantSolicitor1Reference() == null) {
            claimRefNumber = "";
        } else {
            claimRefNumber = caseData.getSolicitorReferences().getApplicantSolicitor1Reference();
        }

        return Map.of(
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        );
    }

    public String addTrialOrHearing(CaseData caseData) {

        if (caseData.getAllocatedTrack() == AllocatedTrack.FAST_CLAIM) {
            return "trial";
        } else {
            return "hearing";
        }
    }

    private Map<String, String> addPropertiesLip(CaseData caseData) {

        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
        );
    }

    private boolean isLipApplicant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.isApplicantNotRepresented();
    }
}
