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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_SPEC;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;

@Service
@RequiredArgsConstructor
public class RaisingClaimAgainstLitigantInPersonForSpecNotificationHandler extends CallbackHandler
    implements NotificationData {

    private static final List<CaseEvent> EVENTS =
        List.of(
            NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON_SPEC
        );
    public static final String TASK_ID = "CreateClaimProceedsOfflineNotifyApplicantSolicitor1ForSpec";
    private static final String REFERENCE_TEMPLATE = "applicant-create-case-handed-offline-notification-%s";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantSolicitorCaseHandedOffline
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

    private CallbackResponse notifyApplicantSolicitorCaseHandedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            caseData.getApplicantSolicitor1UserDetails().getEmail(),
            notificationsProperties.getClaimantSolicitorSpecCaseWillProgressOffline(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            PARTY_REFERENCES, buildPartiesReferences(caseData)
        );
    }
}
