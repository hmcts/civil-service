package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotifyEventHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_EVENT);

    public static final String TASK_ID = "LitigationFriendAddedNotifier";
    private static final String REFERENCE_TEMPLATE = "litigation-friend-added-applicant-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyForLitigationFriendAdded
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

    private CallbackResponse notifyForLitigationFriendAdded(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Entering notifyForLitigationFriendAdded. Case id: {}", callbackParams.getCaseData().getCcdCaseReference());

        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getSolicitorLitigationFriendAdded(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService)
        );
    }
}
