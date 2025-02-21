package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class ClaimSetAsideJudgmentDefendantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1,
                                                          NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT2,
                                                          NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1_LIP);
    private static final String REFERENCE_TEMPLATE =
        "set-aside-judgment-defendant-notification-%s";
    public static final String TASK_ID_RESPONDENT1 = "NotifyDefendantSetAsideJudgment1";
    public static final String TASK_ID_RESPONDENT2 = "NotifyDefendantSetAsideJudgment2";
    public static final String TASK_ID_RESPONDENT1_LIP = "NotifyDefendantSetAsideJudgment1LiP";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimSetAsideJudgmentToDefendant
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1.equals(caseEvent)) {
            return TASK_ID_RESPONDENT1;
        } else if (NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT2.equals(caseEvent)) {
            return TASK_ID_RESPONDENT2;
        } else {
            return TASK_ID_RESPONDENT1_LIP;
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService),
            DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData),
            REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private CallbackResponse notifyClaimSetAsideJudgmentToDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipientEmail = getRecipientEmail(callbackParams);
        boolean isRespondentLip = callbackParams.getRequest().getEventId()
            .equals(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1_LIP.name());

        if (recipientEmail != null) {
            notificationService.sendMail(
                recipientEmail,
                getTemplate(isRespondentLip),
                getEmailProperties(callbackParams, caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private Map<String, String> getEmailProperties(CallbackParams callbackParams, CaseData caseData) {
        if (callbackParams.getRequest().getEventId().equals(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1_LIP.name())) {
            return addPropertiesLip(caseData);
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1.name())) {
            return addProperties(caseData);
        } else {
            return addRespondent2Properties(caseData);
        }
    }

    private String getTemplate(boolean isRespondentLip) {
        return isRespondentLip ? notificationsProperties.getNotifyUpdateTemplate()
            : notificationsProperties.getNotifySetAsideJudgmentTemplate();
    }

    private String getRecipientEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (callbackParams.getRequest().getEventId().equals(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1_LIP.name())) {
            return caseData.getRespondent1().getPartyEmail() != null ? caseData.getRespondent1().getPartyEmail() : null;
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1.name())) {
            return caseData.getRespondentSolicitor1EmailAddress() != null
                ? caseData.getRespondentSolicitor1EmailAddress() : null;
        } else {
            return caseData.getRespondentSolicitor2EmailAddress() != null
                ? caseData.getRespondentSolicitor2EmailAddress() : null;
        }
    }

    private Map<String, String> addRespondent2Properties(final CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService),
            REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText(),
            DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference()
        );
    }

    private Map<String, String> addPropertiesLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData),
            PARTY_NAME, caseData.getRespondent1().getPartyName()
        );
    }
}
