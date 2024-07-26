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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DISCONTINUANCE_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Service
@RequiredArgsConstructor
public class NotifyDefendantClaimDiscontinuedNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_DISCONTINUANCE_DEFENDANT1);
    public static final String TASK_ID = "NotifyDefendantClaimDiscontinued";
    private static final String REFERENCE_TEMPLATE =
        "defendant-claim-discontinued-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::notifyDefendantClaimDiscontinued
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

    private CallbackResponse notifyDefendantClaimDiscontinued(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (!caseData.isRespondent1LiP()) {
            notificationService.sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                getLRTemplate(),
                addProperties(caseData),
                getReferenceTemplate(caseData)
            );
        } else {
            notificationService.sendMail(
                caseData.getRespondent1().getPartyEmail(),
                getLIPTemplate(),
                addPropertiesLip(caseData),
                getReferenceTemplate(caseData)
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String getLRTemplate() {
        return notificationsProperties.getNotifyClaimDiscontinuedLRTemplate();
    }

    private String getLIPTemplate() {
        return notificationsProperties.getNotifyClaimDiscontinuedLipTemplate();
    }

    private String getReferenceTemplate(CaseData caseData) {
        return String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference());
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER,
            caseData.getLegacyCaseReference(),
            LEGAL_ORG_NAME,
            getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService)
        );
    }

    public Map<String, String> addPropertiesLip(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            RESPONDENT_NAME, caseData.getRespondent1().getPartyName()
        );
    }
}
