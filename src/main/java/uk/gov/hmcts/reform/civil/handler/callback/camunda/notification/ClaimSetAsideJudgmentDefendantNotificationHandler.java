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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Service
@RequiredArgsConstructor
public class ClaimSetAsideJudgmentDefendantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1,
                                                          NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT2);
    private static final String REFERENCE_TEMPLATE =
        "set-aside-judgment-defendant-notification-%s";
    public static final String TASK_ID_RESPONDENT1 = "NotifyDefendantSetAsideJudgment1";
    public static final String TASK_ID_RESPONDENT2 = "NotifyDefendantSetAsideJudgment2";

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
        } else {
            return TASK_ID_RESPONDENT2;
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
            REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText()
        );
    }

    private CallbackResponse notifyClaimSetAsideJudgmentToDefendant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        String emailTemplateID = notificationsProperties.getNotifySetAsideJudgmentTemplate();

        notificationService.sendMail(
            callbackParams.getRequest().getEventId().equals(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1.name())
                ? caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress(),
            emailTemplateID,
            callbackParams.getRequest().getEventId().equals(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_DEFENDANT1.name())
                ? addProperties(caseData) : addRespondent2Properties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private Map<String, String> addRespondent2Properties(final CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getRespondentLegalOrganizationName(caseData.getRespondent2OrganisationPolicy(), organisationService),
            REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText(),
            DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData)
        );
    }

}
