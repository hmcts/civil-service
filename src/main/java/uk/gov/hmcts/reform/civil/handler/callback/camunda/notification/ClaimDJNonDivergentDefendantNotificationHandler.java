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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Service
@RequiredArgsConstructor
public class ClaimDJNonDivergentDefendantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR,
                                                          NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR,
                                                          NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP);
    private static final String REFERENCE_TEMPLATE = "dj-non-divergent-defendant-notification-%s";
    private static final String REFERENCE_TEMPLATE_LIP = "dj-non-divergent-defendant-notification-lip-%s";
    private static final String TASK_ID_RESPONDENT1 = "NotifyDJNonDivergentDefendant1";
    private static final String TASK_ID_RESPONDENT2 = "NotifyDJNonDivergentDefendant2";
    private static final String TASK_ID_RESPONDENT1_LIP = "NotifyDJNonDivergentDefendant1LiP";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimDefaultJudgementToDefendant
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR.equals(caseEvent)) {
            return TASK_ID_RESPONDENT1;
        } else if (NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT2_LR.equals(caseEvent)) {
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
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService),
            DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
        );
    }

    private CallbackResponse notifyClaimDefaultJudgementToDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String recipientEmail = getRecipientEmail(callbackParams);
        boolean isRespondentLip = callbackParams.getRequest().getEventId()
            .equals(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP.name());

        if (recipientEmail != null) {
            notificationService.sendMail(
                recipientEmail,
                getTemplate(isRespondentLip),
                getEmailProperties(callbackParams, caseData),
                getReferenceTemplate(caseData, isRespondentLip)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getReferenceTemplate(CaseData caseData, boolean isRespondentLip) {
        return isRespondentLip ? String.format(REFERENCE_TEMPLATE_LIP, caseData.getLegacyCaseReference())
            : String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    private Map<String, String> getEmailProperties(CallbackParams callbackParams, CaseData caseData) {
        if (callbackParams.getRequest().getEventId().equals(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP.name())) {
            return addPropertiesLip(caseData);
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR.name())) {
            return addProperties(caseData);
        } else {
            return addRespondent2Properties(caseData);
        }
    }

    private String getTemplate(boolean isRespondentLip) {
        return isRespondentLip ? notificationsProperties.getNotifyUpdateTemplate()
            : notificationsProperties.getNotifyDJNonDivergentSpecDefendantTemplate();
    }

    private String getRecipientEmail(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (callbackParams.getRequest().getEventId().equals(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LIP.name())) {
            return caseData.getRespondent1().getPartyEmail() != null ? caseData.getRespondent1().getPartyEmail() : null;
        } else if (callbackParams.getRequest().getEventId().equals(NOTIFY_DJ_NON_DIVERGENT_SPEC_DEFENDANT1_LR.name())) {
            return caseData.getRespondentSolicitor1EmailAddress() != null
                ? caseData.getRespondentSolicitor1EmailAddress() : null;
        } else {
            return caseData.getRespondentSolicitor2EmailAddress() != null
                ? caseData.getRespondentSolicitor2EmailAddress() : null;
        }
    }

    private Map<String, String> addRespondent2Properties(final CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService),
            DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName()
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
