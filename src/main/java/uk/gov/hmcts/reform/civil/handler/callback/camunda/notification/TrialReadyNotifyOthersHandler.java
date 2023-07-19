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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class TrialReadyNotifyOthersHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(
        NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY,
        NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY,
        NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY
    );

    public static final String TASK_ID_APPLICANT = "OtherTrialReadyNotifyApplicantSolicitor1";
    public static final String TASK_ID_RESPONDENT_ONE = "OtherTrialReadyNotifyRespondentSolicitor1";
    public static final String TASK_ID_RESPONDENT_TWO = "OtherTrialReadyNotifyRespondentSolicitor2";
    private static final String REFERENCE_TEMPLATE = "other-party-trial-ready-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifySolicitorForOtherTrialReady
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        String eventId = callbackParams.getRequest().getEventId();
        if (eventId.equals(NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY.name())) {
            return TASK_ID_APPLICANT;
        } else if (eventId.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY.name())) {
            return TASK_ID_RESPONDENT_ONE;
        } else {
            return TASK_ID_RESPONDENT_TWO;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse notifySolicitorForOtherTrialReady(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String eventId = callbackParams.getRequest().getEventId();
        String emailAddress;
        boolean isLiP = false;
        boolean isApplicant = false;
        if (eventId.equals(NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY.name())) {
            isApplicant = true;
            isLiP = isLiP(isApplicant, caseData);
            emailAddress = isLiP ? caseData.getApplicant1().getPartyEmail() : caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (eventId.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY.name())) {
            isLiP = isLiP(isApplicant, caseData);
            emailAddress = isLiP ? caseData.getRespondent1EmailAddress() : caseData.getRespondentSolicitor1EmailAddress();
        } else {
            emailAddress = caseData.getRespondentSolicitor2EmailAddress();
            if (null == emailAddress && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
                emailAddress = caseData.getRespondentSolicitor1EmailAddress();
            }
        }

        notificationService.sendMail(
            emailAddress,
            //TODO: replace getRespondent1LipClaimUpdatedTemplate() with getNotifyLipUpdateTemplate() once CIV-9123 is merged into master
            isLiP ? notificationsProperties.getRespondent1LipClaimUpdatedTemplate() : notificationsProperties.getOtherPartyTrialReady(),
            isLiP ? addPropertiesLiP(isApplicant, caseData) : addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private boolean isLiP(boolean isApplicant, CaseData caseData) {
        return isApplicant ? SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isApplicantNotRepresented()
            : SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent1LiP();
    }

    private Map<String, String> addPropertiesLiP(boolean isApplicant, CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_NAME, isApplicant ? caseData.getApplicant1().getPartyName() : caseData.getRespondent1().getPartyName(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData)
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
    }
}
