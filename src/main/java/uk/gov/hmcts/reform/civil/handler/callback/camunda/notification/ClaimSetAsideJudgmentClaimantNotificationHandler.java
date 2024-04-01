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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_CLAIMANT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;

@Service
@RequiredArgsConstructor
public class ClaimSetAsideJudgmentClaimantNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_CLAIM_SET_ASIDE_JUDGMENT_CLAIMANT);
    public static final String TASK_ID = "NotifyClaimantSetAsideJudgment";

    private static final String REFERENCE_TEMPLATE = "set-aside-judgment-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyClaimSetAsideJudgmentToClaimant
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

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService),
            DEFENDANT_NAME_INTERIM,  getDefendantNameBasedOnCaseType(caseData),
            REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText()
        );
    }

    private CallbackResponse notifyClaimSetAsideJudgmentToClaimant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getApplicantSolicitor1UserDetails().getEmail() != null) {
            notificationService.sendMail(
                caseData.getApplicantSolicitor1UserDetails().getEmail(),
                getTemplate(),
                addProperties(caseData),
                getReferenceTemplate(caseData)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getTemplate() {
        return notificationsProperties.getNotifySetAsideJudgmentTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

}
