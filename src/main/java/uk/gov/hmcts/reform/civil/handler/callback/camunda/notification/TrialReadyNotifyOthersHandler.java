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
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
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
    private final OrganisationService organisationService;

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
        String emailAddress = null;
        boolean isLiP;
        boolean isApplicant = false;
        switch (CaseEvent.valueOf(eventId)) {
            case NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY -> {
                isApplicant = true;
                isLiP = isLiP(isApplicant, false, caseData);
                if (isNull(caseData.getTrialReadyApplicant())) {
                    emailAddress = getEmail(isApplicant, false, isLiP, caseData);
                }
            }
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY -> {
                isLiP = isLiP(isApplicant, false, caseData);
                if (isNull(caseData.getTrialReadyRespondent1())) {
                    emailAddress = getEmail(isApplicant, false, isLiP, caseData);
                }
            }
            default -> {
                isLiP = isLiP(isApplicant, true, caseData);
                if (isNull(caseData.getTrialReadyRespondent2())) {
                    emailAddress = getEmail(isApplicant, true, isLiP, caseData);

                    if (null == emailAddress && caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES) {
                        emailAddress = getEmail(isApplicant, false, isLiP, caseData);
                    }
                }
            }
        }

        Map<String, String> properties = new HashMap<>();
        if (!isLiP) {
            properties = addProperties(caseData);
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getOrgName(CaseEvent.valueOf(eventId), caseData));
        }

        if (emailAddress != null && !emailAddress.isEmpty()) {
            notificationService.sendMail(
                emailAddress,
                getTemplate(caseData, isLiP, isApplicant),
                isLiP ? addPropertiesLiP(isApplicant, caseData) : properties,
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getTemplate(CaseData caseData, boolean isLiP, boolean isApplicant) {
        String emailTemplate;
        if (isLiP) {
            if ((isApplicant && caseData.isClaimantBilingual())
                || caseData.isRespondentResponseBilingual()) {
                emailTemplate = notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else {
                emailTemplate = notificationsProperties.getNotifyLipUpdateTemplate();
            }
        } else {
            emailTemplate = notificationsProperties.getOtherPartyTrialReady();
        }
        return emailTemplate;
    }

    private String getEmail(boolean isApplicant, boolean isRespondent2, boolean isLiP, CaseData caseData) {
        String email;
        if (isApplicant) {
            email = isLiP ? caseData.getApplicant1Email() : caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if (isRespondent2) {
            email = isLiP ? caseData.getRespondent2().getPartyEmail() : caseData.getRespondentSolicitor2EmailAddress();
        } else {
            email = isLiP ? caseData.getRespondent1().getPartyEmail() : caseData.getRespondentSolicitor1EmailAddress();
        }
        return email;
    }

    private boolean isLiP(boolean isApplicant, boolean isRespondent2, CaseData caseData) {
        boolean isLiP;
        if (isApplicant) {
            isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isApplicantNotRepresented();
        } else if (isRespondent2) {
            isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent2LiP();
        } else {
            isLiP = SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && caseData.isRespondent1LiP();
        }
        return isLiP;
    }

    private Map<String, String> addPropertiesLiP(boolean isApplicant, CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER,
            caseData.getCcdCaseReference().toString(),
            PARTY_NAME,
            isApplicant ? caseData.getApplicant1().getPartyName() : caseData.getRespondent1().getPartyName(),
            CLAIMANT_V_DEFENDANT,
            getAllPartyNames(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return new HashMap<>(Map.of(
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        ));
    }

    private String getOrgName(CaseEvent eventId, CaseData caseData) {
        switch (eventId) {
            case NOTIFY_APPLICANT_SOLICITOR_FOR_OTHER_TRIAL_READY -> {
                return isLiP(true, false, caseData)
                    ? caseData.getApplicant1().getPartyName() : getApplicantLegalOrganizationName(caseData, organisationService);
            }
            case NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY -> {
                return isLiP(false, false, caseData)
                    ? caseData.getRespondent1().getPartyName()
                    : getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService);
            }
            default -> {
                return isLiP(false, true, caseData)
                    ? caseData.getRespondent2().getPartyName()
                    : getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService);
            }
        }
    }
}
